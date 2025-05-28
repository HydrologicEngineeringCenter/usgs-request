package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class UsgsSiteParserTest {

    @Test
    void parseSites() {
        URL inUrl = Objects.requireNonNull(getClass().getResource(
                "/usgs_sites.rdb"));

        Path path = new File(inUrl.getFile()).toPath();

        try {
            String response = Files.readString(path);
            List<UsgsSite> sites = UsgsSiteParser.parse(response);
            assertEquals(1, sites.size());
            UsgsSite usgsSite = sites.get(0);
            assertEquals("03034000", usgsSite.getNumber());
            assertEquals("Mahoning Creek at Punxsutawney, PA", usgsSite.getName());
            assertEquals(-79.00836738, usgsSite.getLongitude());
            assertEquals(40.93923105, usgsSite.getLatitude());
            assertIterableEquals(List.of(UsgsTimeZoneCode.EST, UsgsTimeZoneCode.EDT), usgsSite.getTimeZones());
        } catch (IOException e) {
            Assertions.fail();
        }
    }
}