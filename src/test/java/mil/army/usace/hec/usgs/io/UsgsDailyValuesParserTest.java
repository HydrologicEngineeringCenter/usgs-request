package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

class UsgsDailyValuesParserTest {

    @Test
    void parseTimeWithUtcOffset() {
        ZonedDateTime result = UsgsDailyValuesParser.parseTime("1951-10-08", ZoneOffset.UTC);
        Assertions.assertEquals(1951, result.getYear());
        Assertions.assertEquals(10, result.getMonthValue());
        Assertions.assertEquals(8, result.getDayOfMonth());
        Assertions.assertEquals(0, result.getHour());
        Assertions.assertEquals(0, result.getMinute());
        Assertions.assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void parseTimeWithNonUtcOffset() {
        // EST is -05:00; daily value should be at start of day in that zone
        ZoneOffset est = ZoneOffset.ofHours(-5);
        ZonedDateTime result = UsgsDailyValuesParser.parseTime("1951-10-08", est);
        Assertions.assertEquals(1951, result.getYear());
        Assertions.assertEquals(10, result.getMonthValue());
        Assertions.assertEquals(8, result.getDayOfMonth());
        Assertions.assertEquals(0, result.getHour());
        Assertions.assertEquals(est, result.getZone());
    }
}
