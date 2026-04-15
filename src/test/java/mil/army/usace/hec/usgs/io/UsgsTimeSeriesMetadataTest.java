package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UsgsTimeSeriesMetadataTest {

    private static UsgsTimeSeriesMetadata meta(ZonedDateTime begin, ZonedDateTime end) {
        return UsgsTimeSeriesMetadata.builder()
                .setId("USGS-" + begin)
                .setBeginTime(begin)
                .setEndTime(end)
                .build();
    }

    private static ZonedDateTime t(int year) {
        return ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    @Test
    void narrowsToEnvelopeWhenQueryWiderThanMetadata() {
        List<UsgsTimeSeriesMetadata> md = List.of(
                meta(t(1990), t(1995)),
                meta(t(1989), t(1996))
        );

        Optional<ZonedDateTime[]> window = UsgsTimeSeriesMetadata.narrowWindow(md, t(1980), t(2000));

        assertTrue(window.isPresent());
        assertEquals(t(1989), window.get()[0]);
        assertEquals(t(1996), window.get()[1]);
    }

    @Test
    void preservesQueryBoundsWhenMetadataWider() {
        List<UsgsTimeSeriesMetadata> md = List.of(meta(t(1970), t(2030)));

        Optional<ZonedDateTime[]> window = UsgsTimeSeriesMetadata.narrowWindow(md, t(1990), t(2000));

        assertTrue(window.isPresent());
        assertEquals(t(1990), window.get()[0]);
        assertEquals(t(2000), window.get()[1]);
    }

    @Test
    void returnsEmptyWhenNoOverlap() {
        List<UsgsTimeSeriesMetadata> md = List.of(meta(t(1970), t(1980)));

        Optional<ZonedDateTime[]> window = UsgsTimeSeriesMetadata.narrowWindow(md, t(1990), t(2000));

        assertTrue(window.isEmpty());
    }

    @Test
    void handlesOpenEndedQuery() {
        List<UsgsTimeSeriesMetadata> md = List.of(meta(t(1990), t(2010)));

        Optional<ZonedDateTime[]> window = UsgsTimeSeriesMetadata.narrowWindow(md, null, null);

        assertTrue(window.isPresent());
        assertEquals(t(1990), window.get()[0]);
        assertEquals(t(2010), window.get()[1]);
    }

    @Test
    void emptyMetadataCollectionReturnsEmpty() {
        assertTrue(UsgsTimeSeriesMetadata.narrowWindow(List.of(), t(1990), t(2000)).isEmpty());
    }
}
