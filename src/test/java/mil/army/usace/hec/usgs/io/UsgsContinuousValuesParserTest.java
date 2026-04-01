package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

class UsgsContinuousValuesParserTest {

    @Test
    void parseTimeWithUtcOffset() {
        ZonedDateTime result = UsgsContinuousValuesParser.parseTime("2026-03-10T12:30:00+00:00");
        Assertions.assertEquals(2026, result.getYear());
        Assertions.assertEquals(3, result.getMonthValue());
        Assertions.assertEquals(10, result.getDayOfMonth());
        Assertions.assertEquals(12, result.getHour());
        Assertions.assertEquals(30, result.getMinute());
        Assertions.assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void parseTimeWithNonUtcOffset() {
        // EDT is -04:00; should be converted to UTC
        ZonedDateTime result = UsgsContinuousValuesParser.parseTime("2022-09-02T17:15:00-04:00");
        Assertions.assertEquals(2022, result.getYear());
        Assertions.assertEquals(9, result.getMonthValue());
        Assertions.assertEquals(2, result.getDayOfMonth());
        Assertions.assertEquals(21, result.getHour());
        Assertions.assertEquals(15, result.getMinute());
        Assertions.assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void parseTimeWithoutOffset() {
        // No offset in the string — should assume UTC
        ZonedDateTime result = UsgsContinuousValuesParser.parseTime("2022-09-02T17:15:00");
        Assertions.assertEquals(2022, result.getYear());
        Assertions.assertEquals(9, result.getMonthValue());
        Assertions.assertEquals(2, result.getDayOfMonth());
        Assertions.assertEquals(17, result.getHour());
        Assertions.assertEquals(15, result.getMinute());
        Assertions.assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void parseTimeWithZSuffix() {
        ZonedDateTime result = UsgsContinuousValuesParser.parseTime("2022-09-02T17:15:00Z");
        Assertions.assertEquals(17, result.getHour());
        Assertions.assertEquals(ZoneOffset.UTC, result.getZone());
    }
}
