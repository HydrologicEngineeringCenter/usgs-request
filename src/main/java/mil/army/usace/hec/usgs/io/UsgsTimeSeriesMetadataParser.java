package mil.army.usace.hec.usgs.io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsgsTimeSeriesMetadataParser {
    private static final Logger LOGGER = Logger.getLogger(UsgsTimeSeriesMetadataParser.class.getName());

    /*
     * JSON property names – keep them in one place so typos are impossible.
     */
    private static final String NODE_FEATURES = "features";
    private static final String NODE_PROPERTIES = "properties";
    private static final String PROP_MONITORING_LOCATION_ID = "monitoring_location_id";
    private static final String PROP_STATISTIC_ID = "statistic_id";
    private static final String PROP_BEGIN = "begin";
    private static final String PROP_END = "end";

    private static final DateTimeFormatter DATE_TIME_FMT =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .optionalEnd()
                    .toFormatter();

    private UsgsTimeSeriesMetadataParser() {
    }

    public static List<UsgsTimeSeriesMetadata> parse(String json) {
        if (json == null)
            return Collections.emptyList();

        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject root = (JSONObject) jsonParser.parse(json);
            JSONArray features = (JSONArray) root.get(NODE_FEATURES);
            if (features == null)
                return Collections.emptyList();

            List<UsgsTimeSeriesMetadata> out = new ArrayList<>(features.size());

            for (Object f : features) {
                JSONObject feature = (JSONObject) f;

                /* ------------- properties --------------------------- */
                JSONObject props = (JSONObject) feature.get(NODE_PROPERTIES);
                if (props == null) 
                    continue;

                String id = (String) props.get(PROP_MONITORING_LOCATION_ID);
                String statistic = (String) props.get(PROP_STATISTIC_ID);
                String beginTxt = (String) props.get(PROP_BEGIN);
                String endTxt = (String) props.get(PROP_END);

                ZonedDateTime begin = parseUtc(beginTxt);
                ZonedDateTime end = parseUtc(endTxt);

                UsgsTimeSeriesMetadata ts =
                        UsgsTimeSeriesMetadata.builder()
                                .setId(id)
                                .setStatisticId(statistic)
                                .setBeginTime(begin)
                                .setEndTime(end)
                                .build();

                out.add(ts);
            }
            return out;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
        }
        return Collections.emptyList();
    }

    /**
     * Helper that turns the USGS timestamp (no zone information) into
     * a ZonedDateTime in UTC so that the model remains fully qualified.
     */
    private static ZonedDateTime parseUtc(String txt) {
        if (txt == null || txt.isBlank()) {
            return null;
        }
        try {
            // Try offset-aware format first (e.g. 2024-01-01T00:00:00+00:00 or Z)
            return OffsetDateTime.parse(txt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .atZoneSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            // Fall back to local datetime without offset — assume UTC
            return ZonedDateTime.of(
                    LocalDateTime.parse(txt, DATE_TIME_FMT),
                    ZoneOffset.UTC);
        }
    }
}
