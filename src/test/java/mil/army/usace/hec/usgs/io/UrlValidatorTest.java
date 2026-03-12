package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    @Test
    void validGovHttpsUrl() {
        String url = "https://api.waterdata.usgs.gov/ogcapi/v0/collections/daily/items?f=json";
        assertEquals(url, UrlValidator.validate(url));
    }

    @Test
    void validGovHttpUrl() {
        String url = "http://api.waterdata.usgs.gov/ogcapi/v0/collections/daily/items?f=json";
        assertEquals(url, UrlValidator.validate(url));
    }

    @Test
    void rejectsNonGovDomain() {
        assertThrows(IllegalArgumentException.class, () ->
                UrlValidator.validate("https://example.com/api"));
    }

    @Test
    void rejectsFtpProtocol() {
        assertThrows(IllegalArgumentException.class, () ->
                UrlValidator.validate("ftp://api.waterdata.usgs.gov/data"));
    }

    @Test
    void rejectsMalformedUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                UrlValidator.validate("not-a-url"));
    }

    @Test
    void rejectsEmptyString() {
        assertThrows(IllegalArgumentException.class, () ->
                UrlValidator.validate(""));
    }

    @Test
    void rejectsNullUrl() {
        assertThrows(Exception.class, () ->
                UrlValidator.validate(null));
    }

    @Test
    void rejectsLocalhostWithGovSuffix() {
        // Hostname must end with .gov AND not resolve to a private address
        assertThrows(IllegalArgumentException.class, () ->
                UrlValidator.validate("https://localhost.gov/api"));
    }
}
