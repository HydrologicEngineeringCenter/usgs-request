package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class UsgsTimeZoneCodeTest {

    @Test
    void parseByCode() {
        UsgsTimeZoneCode est = UsgsTimeZoneCode.parse("EST");
        assertEquals(UsgsTimeZoneCode.EST, est);
        assertEquals(ZoneOffset.ofHours(-5), est.getZoneOffset());
    }

    @Test
    void parseByCaseInsensitiveCode() {
        assertEquals(UsgsTimeZoneCode.EST, UsgsTimeZoneCode.parse("est"));
    }

    @Test
    void parseByName() {
        assertEquals(UsgsTimeZoneCode.EST, UsgsTimeZoneCode.parse("Eastern Standard Time"));
    }

    @Test
    void parseNull() {
        assertEquals(UsgsTimeZoneCode.UNDEFINED, UsgsTimeZoneCode.parse(null));
    }

    @Test
    void parseBlank() {
        assertEquals(UsgsTimeZoneCode.UNDEFINED, UsgsTimeZoneCode.parse("  "));
    }

    @Test
    void parseUnknownReturnsUndefined() {
        assertEquals(UsgsTimeZoneCode.UNDEFINED, UsgsTimeZoneCode.parse("XYZ"));
    }

    @Test
    void isDstReturnsTrueForDaylightSavings() {
        assertTrue(UsgsTimeZoneCode.EDT.isDst());
        assertTrue(UsgsTimeZoneCode.CDT.isDst());
        assertTrue(UsgsTimeZoneCode.PDT.isDst());
    }

    @Test
    void isDstReturnsFalseForStandard() {
        assertFalse(UsgsTimeZoneCode.EST.isDst());
        assertFalse(UsgsTimeZoneCode.CST.isDst());
        assertFalse(UsgsTimeZoneCode.PST.isDst());
    }

    @Test
    void getDstReturnsCorrectMapping() {
        assertEquals(UsgsTimeZoneCode.EDT, UsgsTimeZoneCode.EST.getDst());
        assertEquals(UsgsTimeZoneCode.CDT, UsgsTimeZoneCode.CST.getDst());
        assertEquals(UsgsTimeZoneCode.PDT, UsgsTimeZoneCode.PST.getDst());
    }

    @Test
    void getDstReturnsNullForUnmapped() {
        assertNull(UsgsTimeZoneCode.UTC.getDst());
        assertNull(UsgsTimeZoneCode.UNDEFINED.getDst());
    }

    @Test
    void utcHasZeroOffset() {
        assertEquals(ZoneOffset.UTC, UsgsTimeZoneCode.UTC.getZoneOffset());
    }

    @Test
    void parseByToStringRepresentation() {
        assertEquals(UsgsTimeZoneCode.UTC, UsgsTimeZoneCode.parse("(00:00) Universal Coordinated Time"));
        assertEquals(UsgsTimeZoneCode.EST, UsgsTimeZoneCode.parse("(-05:00) Eastern Standard Time"));
    }
}
