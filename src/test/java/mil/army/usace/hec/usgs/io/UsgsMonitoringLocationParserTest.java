package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsgsMonitoringLocationParserTest {

    @Test
    void parse() throws IOException {
        URL url = Objects.requireNonNull(getClass().getResource("/usgs_monitoring_location.json"));
        String json = Files.readString(new File(url.getFile()).toPath());

        List<UsgsMonitoringLocation> locations = UsgsMonitoringLocationParser.parse(json);

        assertEquals(10, locations.size());

        UsgsMonitoringLocation first = locations.get(0);
        assertEquals("USGS", first.getAgencyCode());
        assertEquals("01540750", first.getMonitoringLocationNumber());
        assertEquals("WB Susquehanna River at McGees Mills, PA", first.getName());
        assertEquals(-78.7644747846451, first.getLongitude(), 1e-6);
        assertEquals(40.8797858628619, first.getLatitude(), 1e-6);
    }

    @Test
    void parseNull() {
        List<UsgsMonitoringLocation> locations = UsgsMonitoringLocationParser.parse(null);
        assertTrue(locations.isEmpty());
    }

    @Test
    void parseEmptyFeatures() {
        String json = "{\"type\":\"FeatureCollection\",\"features\":[]}";
        List<UsgsMonitoringLocation> locations = UsgsMonitoringLocationParser.parse(json);
        assertTrue(locations.isEmpty());
    }
}
