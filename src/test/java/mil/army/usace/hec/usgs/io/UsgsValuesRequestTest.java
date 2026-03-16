package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsgsValuesRequestTest {

    private static UsgsMonitoringLocation testLocation() {
        return UsgsMonitoringLocation.from("USGS-03034000");
    }

    private static UsgsValuesRequest.Builder validBuilder() {
        return UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(testLocation())
                .setParameter(UsgsParameter.DISCHARGE_CFS);
    }

    @Test
    void buildThrowsWhenServiceNull() {
        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .addMonitoringLocation(testLocation())
                        .setParameter(UsgsParameter.DISCHARGE_CFS)
                        .build());
    }

    @Test
    void buildThrowsWhenNoMonitoringLocations() {
        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .setService(UsgsService.CONTINUOUS)
                        .setParameter(UsgsParameter.DISCHARGE_CFS)
                        .build());
    }

    @Test
    void buildThrowsWhenParameterNull() {
        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .setService(UsgsService.CONTINUOUS)
                        .addMonitoringLocation(testLocation())
                        .build());
    }

    @Test
    void buildThrowsWhenDurationAndTimeBothSet() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        assertThrows(IllegalStateException.class, () ->
                validBuilder()
                        .setDuration(Duration.ofHours(3))
                        .setBeginTime(begin)
                        .build());
    }

    @Test
    void buildThrowsWhenEndBeforeBegin() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        assertThrows(IllegalStateException.class, () ->
                validBuilder()
                        .setBeginTime(begin)
                        .setEndTime(end)
                        .build());
    }

    @Test
    void buildThrowsWhenEndEqualsBegin() {
        ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        assertThrows(IllegalStateException.class, () ->
                validBuilder()
                        .setBeginTime(time)
                        .setEndTime(time)
                        .build());
    }

    @Test
    void buildThrowsWhenDurationNegative() {
        assertThrows(IllegalStateException.class, () ->
                validBuilder()
                        .setDuration(Duration.ofHours(-3))
                        .build());
    }

    @Test
    void buildThrowsWhenDurationZero() {
        assertThrows(IllegalStateException.class, () ->
                validBuilder()
                        .setDuration(Duration.ZERO)
                        .build());
    }

    @Test
    void toStringFormatsDuration() {
        UsgsValuesRequest request = validBuilder()
                .setDuration(Duration.ofHours(3))
                .build();

        assertTrue(request.toString().contains("&time=PT3H"));
    }

    @Test
    void toStringFormatsTimeRange() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = ZonedDateTime.of(2024, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsValuesRequest request = validBuilder()
                .setBeginTime(begin)
                .setEndTime(end)
                .build();

        String url = request.toString();
        assertTrue(url.contains("&time=2024-01-01T00:00:00Z/2024-02-01T00:00:00Z"));
    }

    @Test
    void toStringFormatsBeginOnly() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsValuesRequest request = validBuilder()
                .setBeginTime(begin)
                .build();

        assertTrue(request.toString().contains("&time=2024-01-01T00:00:00Z/.."));
    }

    @Test
    void toStringFormatsEndOnly() {
        ZonedDateTime end = ZonedDateTime.of(2024, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.DAILY)
                .addMonitoringLocation(testLocation())
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setEndTime(end)
                .build();

        assertTrue(request.toString().contains("&time=../2024-02-01T00:00:00Z"));
    }

    @Test
    void toStringNormalizesNonUtcTimesToUtc() {
        ZonedDateTime estBegin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.of("America/New_York"));
        ZonedDateTime estEnd = ZonedDateTime.of(2024, 2, 1, 0, 0, 0, 0, ZoneId.of("America/New_York"));

        UsgsValuesRequest request = validBuilder()
                .setBeginTime(estBegin)
                .setEndTime(estEnd)
                .build();

        String url = request.toString();
        // EST is UTC-5, so midnight EST = 05:00 UTC
        assertTrue(url.contains("&time=2024-01-01T05:00:00Z/2024-02-01T05:00:00Z"));
    }
}