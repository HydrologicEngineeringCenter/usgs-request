package mil.army.usace.hec.usgs.io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for parsing JSON responses from the USGS (United States Geological Survey)
 * to retrieve time series data related to gage readings. This class parses the JSON data
 * and converts it into a list of {@link UsgsGageRecord} objects, which can then be
 * used for hydrological modeling and analysis.
 *
 * <p>This class includes static methods and is not meant to be instantiated.
 * It relies on the JSON-simple library for parsing JSON data.</p>
 */
public class UsgsGageParser {
    private static final Logger logger = Logger.getLogger(UsgsGageParser.class.getName());

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private UsgsGageParser() {
    }

    /**
     * Parses a JSON response string from the USGS API to extract time series data.
     * Converts the JSON time series data into a list of {@link UsgsGageRecord} objects,
     * each representing a time series with timestamps and corresponding values.
     * <p>
     * The TimeSeriesContainer::location field corresponds to the USGS site number, e.g. for the
     * Mahoning Creek at Punxsutawney, PA gage, the USGS site number is 03034000.
     *
     * @param response the JSON response from the USGS API as a string.
     * @return a list of {@link UsgsGageRecord} objects representing the parsed time series data.
     * Returns an empty list if the parsing fails.
     */
    public static UsgsGageRecords parse(String response) {
        List<UsgsGageRecord> usgsGageRecords = new ArrayList<>();
        try {
            JSONObject obj = (JSONObject) new JSONParser().parse(response);
            JSONObject valueObj = (JSONObject) obj.get("value");

            JSONArray timeSeries = (JSONArray) valueObj.get("timeSeries");
            for (Object t : timeSeries) {
                JSONObject sourceInfo = (JSONObject) t;
                JSONObject sourceInfo0 = (JSONObject) sourceInfo.get("sourceInfo");

                // Site name
                String nameString = (String) sourceInfo0.get("siteName");

                // Site code
                JSONArray siteCode = (JSONArray) sourceInfo0.get("siteCode");
                JSONObject siteCode0 = (JSONObject) siteCode.get(0);
                String codeString = (String) siteCode0.get("value");

                // Geolocation
                JSONObject geoLocation = (JSONObject) sourceInfo0.get("geoLocation");
                JSONObject geogLocation = (JSONObject) geoLocation.get("geogLocation");
                double longitude = (double) geogLocation.get("longitude");
                double latitude = (double) geogLocation.get("latitude");

                // Time zone
                JSONObject timeZoneInfo = (JSONObject) sourceInfo0.get("timeZoneInfo");
                JSONObject defaultTimeZone = (JSONObject) timeZoneInfo.get("defaultTimeZone");
                String zoneAbbr = (String) defaultTimeZone.get("zoneAbbreviation");
                UsgsTimeZoneCode timeZone = UsgsTimeZoneCode.parse(zoneAbbr);
                boolean isObservesDST = (Boolean) timeZoneInfo.get("siteUsesDaylightSavingsTime");

                List<UsgsTimeZoneCode> timeZones = new ArrayList<>();
                timeZones.add(timeZone);

                if (isObservesDST) {
                    Optional.ofNullable(timeZone.getDst()).ifPresent(timeZones::add);
                }

                UsgsSite usgsSite = UsgsSite.builder()
                        .setName(nameString)
                        .setId(codeString)
                        .setLongitude(longitude)
                        .setLatitude(latitude)
                        .setTimeZones(timeZones)
                        .build();

                // Variable
                JSONObject variable = (JSONObject) sourceInfo.get("variable");
                JSONArray variableCode = (JSONArray) variable.get("variableCode");
                JSONObject variableCode0 = (JSONObject) variableCode.get(0);
                String variableCodeValue = (String) variableCode0.get("value");
                UsgsParameter parameter = UsgsParameter.fromCode(variableCodeValue);
                double noDataValue = parseDouble(String.valueOf(variable.get("noDataValue")));

                // Values
                JSONArray valuesArray = (JSONArray) sourceInfo.get("values");
                JSONObject value = (JSONObject) valuesArray.get(0);
                JSONArray valueArray = (JSONArray) value.get("value");

                int size = valueArray.size();

                ZonedDateTime[] times = new ZonedDateTime[size];
                double[] values = new double[size];

                for (int i = 0; i < size; i++) {
                    JSONObject item = (JSONObject) valueArray.get(i);
                    String dateString = String.valueOf(item.get("dateTime"));
                    ZonedDateTime dateParsed = parseDate(dateString);
                    times[i] = dateParsed;

                    String valueString = String.valueOf(item.get("value"));
                    double valueParsed = Double.parseDouble(valueString);
                    values[i] = valueParsed;
                }

                UsgsGageRecord usgsGageRecord = UsgsGageRecord.builder()
                        .setSite(usgsSite)
                        .setTimes(times)
                        .setValues(values)
                        .setNoDataValue(noDataValue)
                        .setParameter(parameter)
                        .build();

                usgsGageRecords.add(usgsGageRecord);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e, e::getMessage);
        }
        return UsgsGageRecords.from(usgsGageRecords);
    }

    private static ZonedDateTime parseDate(String dateString) {
        int length = dateString.length();
        // If length is 29 date string includes time zone offset. Parse with ZonedDateTime.
        // Else length is assumed to be 23 with not time zone offset. Parse with LocalDateTime.
        if (length == 29) {
            return ZonedDateTime.parse(dateString);
        } else {
            LocalDateTime ldt = LocalDateTime.parse(dateString);
            return ZonedDateTime.of(ldt, ZoneId.of("UTC"));
        }
    }

    private static double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, e, e::getMessage);
        }
        return -999999;
    }
}
