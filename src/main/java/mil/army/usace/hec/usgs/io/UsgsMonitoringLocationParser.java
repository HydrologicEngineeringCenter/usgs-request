package mil.army.usace.hec.usgs.io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsgsMonitoringLocationParser {
    private static final Logger LOGGER = Logger.getLogger(UsgsMonitoringLocationParser.class.getName());

    private static final String PROP_MONITORING_LOCATION_NUMBER = "monitoring_location_number";
    private static final String PROP_MONITORING_LOCATION_NAME = "monitoring_location_name";
    private static final String PROP_AGENCY_CODE = "agency_code";
    private static final String PROP_TIME_ZONE_ABBREVIATION = "time_zone_abbreviation";
    private static final String PROP_USES_DAYLIGHT_SAVINGS = "uses_daylight_savings";

    private UsgsMonitoringLocationParser() {
    }

    public static List<UsgsMonitoringLocation> parse(String json) {
        if (json == null)
            return Collections.emptyList();

        JSONParser parser = new JSONParser();
        try {
            JSONObject root = (JSONObject) parser.parse(json);
            JSONArray features = (JSONArray) root.get("features");
            if (features == null)
                return Collections.emptyList();

            List<UsgsMonitoringLocation> result = new ArrayList<>(features.size());

            for (Object featureObj : features) {
                JSONObject feature = (JSONObject) featureObj;

                /* ---- geometry ------------------------------------------------- */
                JSONObject geometry = (JSONObject) feature.get("geometry");
                double longitude = Double.NaN;
                double latitude = Double.NaN;
                if (geometry != null) {
                    JSONArray coordinates = (JSONArray) geometry.get("coordinates");
                    if (coordinates != null && coordinates.size() >= 2) {
                        longitude = ((Number) coordinates.get(0)).doubleValue();
                        latitude = ((Number) coordinates.get(1)).doubleValue();
                    }
                }

                /* ---- properties ---------------------------------------------- */
                JSONObject properties = (JSONObject) feature.get("properties");
                if (properties == null)
                    continue;

                String id = (String) properties.get(PROP_MONITORING_LOCATION_NUMBER);
                String name = (String) properties.get(PROP_MONITORING_LOCATION_NAME);
                String agencyCode = (String) properties.get(PROP_AGENCY_CODE);
                String tzAbbr = (String) properties.get(PROP_TIME_ZONE_ABBREVIATION);
                String usesDst = (String) properties.get(PROP_USES_DAYLIGHT_SAVINGS);

                List<UsgsTimeZoneCode> timeZones = new ArrayList<>();
                if (tzAbbr != null) {
                    UsgsTimeZoneCode stdZone = UsgsTimeZoneCode.parse(tzAbbr);
                    timeZones.add(stdZone);
                    if ("Y".equalsIgnoreCase(usesDst)) {
                        UsgsTimeZoneCode dstZone = stdZone.getDst();
                        if (dstZone != null) {
                            timeZones.add(dstZone);
                        }
                    }
                }

                /* ---- build the domain object ---------------------------------- */
                UsgsMonitoringLocation location = UsgsMonitoringLocation.builder()
                        .setMonitoringLocationNumber(id)
                        .setName(name)
                        .setAgencyCode(agencyCode)
                        .setLongitude(longitude)
                        .setLatitude(latitude)
                        .setTimeZones(timeZones)
                        .build();

                result.add(location);
            }
            return result;
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
        }
        return Collections.emptyList();
    }
}
