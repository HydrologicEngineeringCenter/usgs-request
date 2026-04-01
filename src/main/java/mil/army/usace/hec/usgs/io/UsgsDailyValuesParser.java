package mil.army.usace.hec.usgs.io;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class UsgsDailyValuesParser {

    private UsgsDailyValuesParser() {
    }

    /**
     * Parse a date-only string into a {@link ZonedDateTime} at start of day in the given zone.
     * Daily values from the USGS API do not include time zone information;
     * the zone offset should reflect the gage's local time zone.
     */
    static ZonedDateTime parseTime(String s, ZoneOffset zoneOffset) {
        return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(zoneOffset);
    }
}
