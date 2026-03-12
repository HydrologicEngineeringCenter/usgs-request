package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsgsValuesRequestTest {

    private static UsgsMonitoringLocation testLocation() {
        return UsgsMonitoringLocation.from("USGS-03034000");
    }

    @Test
    void buildThrowsWhenServiceNull() {
        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .addMonitoringLocation(testLocation())
                        .build());
    }

    @Test
    void buildThrowsWhenNoMonitoringLocations() {
        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .setService(UsgsService.CONTINUOUS)
                        .build());
    }

    @Test
    void buildThrowsWhenDurationAndTimeBothSet() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .setService(UsgsService.CONTINUOUS)
                        .addMonitoringLocation(testLocation())
                        .setDuration(Duration.ofHours(3))
                        .setBeginTime(begin)
                        .build());
    }

    @Test
    void buildThrowsWhenEndBeforeBegin() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .setService(UsgsService.CONTINUOUS)
                        .addMonitoringLocation(testLocation())
                        .setBeginTime(begin)
                        .setEndTime(end)
                        .build());
    }

    @Test
    void buildThrowsWhenEndEqualsBegin() {
        ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .setService(UsgsService.CONTINUOUS)
                        .addMonitoringLocation(testLocation())
                        .setBeginTime(time)
                        .setEndTime(time)
                        .build());
    }

    @Test
    void buildThrowsWhenDurationNegative() {
        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .setService(UsgsService.CONTINUOUS)
                        .addMonitoringLocation(testLocation())
                        .setDuration(Duration.ofHours(-3))
                        .build());
    }

    @Test
    void buildThrowsWhenDurationZero() {
        assertThrows(IllegalStateException.class, () ->
                UsgsValuesRequest.builder()
                        .setService(UsgsService.CONTINUOUS)
                        .addMonitoringLocation(testLocation())
                        .setDuration(Duration.ZERO)
                        .build());
    }

    @Test
    void toStringFormatsDuration() {
        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(testLocation())
                .setDuration(Duration.ofHours(3))
                .build();

        assertTrue(request.toString().contains("&time=PT3H"));
    }

    @Test
    void toStringFormatsTimeRange() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = ZonedDateTime.of(2024, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(testLocation())
                .setBeginTime(begin)
                .setEndTime(end)
                .build();

        String url = request.toString();
        assertTrue(url.contains("&time=" + begin + "/" + end));
    }

    @Test
    void toStringFormatsBeginOnly() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(testLocation())
                .setBeginTime(begin)
                .build();

        assertTrue(request.toString().contains("&time=" + begin + "/.."));
    }

    @Test
    void toStringFormatsEndOnly() {
        ZonedDateTime end = ZonedDateTime.of(2024, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.DAILY)
                .addMonitoringLocation(testLocation())
                .setEndTime(end)
                .build();

        assertTrue(request.toString().contains("&time=../" + end));
    }
}
