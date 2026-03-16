package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsgsTimeSeriesMetadataParserTest {

    @Test
    void parse() throws IOException {
        URL url = Objects.requireNonNull(getClass().getResource("/usgs_time_series_metadata.json"));
        String json = Files.readString(new File(url.getFile()).toPath());

        List<UsgsTimeSeriesMetadata> metadata = UsgsTimeSeriesMetadataParser.parse(json);

        assertEquals(1, metadata.size());

        UsgsTimeSeriesMetadata first = metadata.get(0);
        assertEquals("USGS-03034000", first.getId());
        assertEquals("00011", first.getStatisticId());
        assertEquals(
                ZonedDateTime.of(2005, 12, 26, 0, 52, 0, 1000, ZoneOffset.UTC),
                first.getBeginTime());
        assertEquals(
                ZonedDateTime.of(2025, 7, 14, 8, 45, 0, 1000, ZoneOffset.UTC),
                first.getEndTime());
    }
}
