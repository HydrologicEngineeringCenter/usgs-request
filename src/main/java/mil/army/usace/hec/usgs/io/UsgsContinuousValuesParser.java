package mil.army.usace.hec.usgs.io;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class UsgsContinuousValuesParser {

    private UsgsContinuousValuesParser() {
    }

    /**
     * Parses a continuous (instantaneous) time string into a {@link ZonedDateTime} in UTC.
     * If the string includes a zone offset, it is converted to UTC.
     * If no offset is present, UTC is assumed.
     */
    static ZonedDateTime parseTime(String s) {
        if (hasOffset(s)) {
            return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .atZoneSameInstant(ZoneOffset.UTC);
        }
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneOffset.UTC);
    }

    private static boolean hasOffset(String s) {
        // Check for 'Z' or '+'/'-' after the time portion (after 'T')
        int tIndex = s.indexOf('T');
        if (tIndex < 0) {
            return false;
        }
        String timePart = s.substring(tIndex);
        return timePart.contains("Z") || timePart.contains("+") || timePart.lastIndexOf('-') > 0;
    }
}
