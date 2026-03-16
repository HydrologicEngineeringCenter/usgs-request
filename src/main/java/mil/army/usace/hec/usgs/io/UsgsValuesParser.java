package mil.army.usace.hec.usgs.io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        if (json == null)
            return UsgsGageRecords.from(Collections.emptyList());

        // Use a composite key to store time-value pairs by site and parameter
        Map<SiteParameterKey, List<TimeValuePair>> timeValuePairsBySiteAndParameter = new LinkedHashMap<>();

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
                ZonedDateTime zonedDateTime = parseToZonedDateTime(timeStr);
                double value = parseValue(valueStr);

                // Create a key for this site and parameter combination
                SiteParameterKey key = new SiteParameterKey(monitoringLocationId, parameterCode);

                // Store time-value pair by site and parameter
                timeValuePairsBySiteAndParameter.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new TimeValuePair(zonedDateTime, value));
            }
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
        }

        // Create UsgsGageRecords grouped by site
        List<UsgsGageRecord> usgsGageRecords = new ArrayList<>();

        for (Map.Entry<SiteParameterKey, List<TimeValuePair>> entry : timeValuePairsBySiteAndParameter.entrySet()) {
            try {
                SiteParameterKey key = entry.getKey();
                String monitoringLocationId = key.monitoringLocationId;
                String parameterCode = key.parameterCode;

                List<TimeValuePair> pairs = entry.getValue();

                // Sort pairs by time (chronological order)
                Collections.sort(pairs);

                // Create separate arrays for times and values
                ZonedDateTime[] timesArray = new ZonedDateTime[pairs.size()];
                double[] valuesArray = new double[pairs.size()];

                for (int i = 0; i < pairs.size(); i++) {
                    TimeValuePair pair = pairs.get(i);
                    timesArray[i] = pair.time;
                    valuesArray[i] = pair.value;
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

    private static ZonedDateTime parseToZonedDateTime(String s) {
        if (s.indexOf('T') < 0) {
            // date-only -> assume UTC at start of day
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(ZoneOffset.UTC);
        }

        try {
            // has time with an offset like +00:00 or Z
            return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .atZoneSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            // has time but no offset — assume UTC
            return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneOffset.UTC);
        }
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

    /**
     * Helper class to keep time and value pairs together for sorting
     */
    private record TimeValuePair(ZonedDateTime time, double value) implements Comparable<TimeValuePair> {

        @Override
        public int compareTo(TimeValuePair other) {
            return this.time.compareTo(other.time);
        }
    }
}
