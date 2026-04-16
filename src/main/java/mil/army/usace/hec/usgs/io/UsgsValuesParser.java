package mil.army.usace.hec.usgs.io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsgsValuesParser {
    private static final Logger LOGGER = Logger.getLogger(UsgsValuesParser.class.getName());

    private static final double DEFAULT_NO_DATA_VALUE = Double.NaN;

    /**
     * Parse USGS values JSON data from a string
     *
     * @param json The string containing JSON data
     * @return UsgsGageRecords parsed from the JSON data
     */
    public static UsgsGageRecords parse(String json) {
        return parse(json, ZoneOffset.UTC);
    }

    /**
     * Parse USGS values JSON data from a string
     *
     * @param json       The string containing JSON data
     * @param zoneOffset The time zone offset to apply when the time string does not include
     *                   time zone information, such as date-only strings from daily data.
     *                   Timestamps that already include an offset (e.g. continuous data) are
     *                   unaffected by this parameter.
     * @return UsgsGageRecords parsed from the JSON data
     */
    public static UsgsGageRecords parse(String json, ZoneOffset zoneOffset) {
        if (json == null)
            return UsgsGageRecords.from(Collections.emptyList());

        // USGS can return the same (site, parameter, timestamp) under multiple time_series_ids,
        // so collect into a TreeMap per key to deduplicate by timestamp and keep chronological order.
        Map<SiteParameterKey, NavigableMap<ZonedDateTime, Double>> valuesByTimeBySiteAndParameter = new LinkedHashMap<>();

        try {
            JSONParser parser = new JSONParser();
            JSONObject rootObject = (JSONObject) parser.parse(json);

            JSONArray features = (JSONArray) rootObject.get("features");
            if (features == null)
                return UsgsGageRecords.from(Collections.emptyList());

            for (Object featureObj : features) {
                JSONObject feature = (JSONObject) featureObj;
                JSONObject properties = (JSONObject) feature.get("properties");

                if (properties == null) continue;

                String monitoringLocationId = (String) properties.get("monitoring_location_id");

                if (monitoringLocationId == null || !monitoringLocationId.contains("-")) {
                    LOGGER.warning(() -> "Skipping feature with invalid monitoring location ID: " + sanitizeForLog(monitoringLocationId));
                    continue;
                }

                String timeStr = (String) properties.get("time");
                String valueStr = (String) properties.get("value");
                String parameterCode = (String) properties.get("parameter_code");

                if (timeStr == null || timeStr.isBlank()) {
                    LOGGER.warning(() -> "Skipping feature with null/blank time for location: " + sanitizeForLog(monitoringLocationId));
                    continue;
                }

                // Parse time and value
                ZonedDateTime zonedDateTime = parseToZonedDateTime(timeStr, zoneOffset);
                double value = parseValue(valueStr);

                // Create a key for this site and parameter combination
                SiteParameterKey key = new SiteParameterKey(monitoringLocationId, parameterCode);

                valuesByTimeBySiteAndParameter.computeIfAbsent(key, k -> new TreeMap<>())
                        .put(zonedDateTime, value);
            }
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
        }

        // Create UsgsGageRecords grouped by site
        List<UsgsGageRecord> usgsGageRecords = new ArrayList<>();

        for (Map.Entry<SiteParameterKey, NavigableMap<ZonedDateTime, Double>> entry : valuesByTimeBySiteAndParameter.entrySet()) {
            try {
                SiteParameterKey key = entry.getKey();
                String monitoringLocationId = key.monitoringLocationId;
                String parameterCode = key.parameterCode;

                NavigableMap<ZonedDateTime, Double> valuesByTime = entry.getValue();

                ZonedDateTime[] timesArray = valuesByTime.keySet().toArray(new ZonedDateTime[0]);
                double[] valuesArray = new double[valuesByTime.size()];
                int i = 0;
                for (Double v : valuesByTime.values()) {
                    valuesArray[i++] = v;
                }

                // Create UsgsMonitoringLocation and UsgsParameter
                UsgsMonitoringLocation usgsMonitoringLocation = UsgsMonitoringLocation.from(monitoringLocationId);
                UsgsParameter parameter = UsgsParameter.fromCode(parameterCode);

                // Build the record for this usgsMonitoringLocation and parameter
                UsgsGageRecord record = UsgsGageRecord.builder()
                        .setMonitoringLocation(usgsMonitoringLocation)
                        .setTimes(timesArray)
                        .setValues(valuesArray)
                        .setNoDataValue(DEFAULT_NO_DATA_VALUE)
                        .setParameter(parameter)
                        .build();

                usgsGageRecords.add(record);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Skipping record due to error: " + e.getMessage(), e);
            }
        }

        return UsgsGageRecords.from(usgsGageRecords);
    }

    private static ZonedDateTime parseToZonedDateTime(String s, ZoneOffset zoneOffset) {
        if (s.indexOf('T') < 0) {
            return UsgsDailyValuesParser.parseTime(s, zoneOffset);
        }
        return UsgsContinuousValuesParser.parseTime(s);
    }

    private static double parseValue(String valueStr) {
        if (valueStr == null)
            return DEFAULT_NO_DATA_VALUE;

        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return DEFAULT_NO_DATA_VALUE;
        }
    }

    private static String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n]", "_");
    }

    /**
     * Composite key for site ID and parameter code
     */
    private record SiteParameterKey(String monitoringLocationId, String parameterCode) {
    }
}
