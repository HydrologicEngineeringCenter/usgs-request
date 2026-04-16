package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsgsContinuousValuesRequestTest {

    @Disabled("Live API smoke test")
    @Test
    void invalidApiKeyRejectedByServer() {
        String fakeKey = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";
        assertTrue(UsgsApiKeyValidator.isValid(fakeKey));

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .addMonitoringLocation(UsgsMonitoringLocation.from("USGS-03034000"))
                .setService(UsgsService.CONTINUOUS)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setDuration(Duration.ofHours(3))
                .setApiKey(fakeKey)
                .build();

        List<String> errorMessages = new ArrayList<>();
        PropertyChangeListener pcl = evt -> {
            if ("error".equalsIgnoreCase(evt.getPropertyName()) && evt.getNewValue() != null) {
                errorMessages.add(String.valueOf(evt.getNewValue()));
            }
        };

        request.addPropertyChangeListener(pcl);

        assertThrows(UsgsRequestException.class, request::retrieve);
        assertFalse(errorMessages.isEmpty());
    }

    @Disabled("Live API smoke test")
    @Test
    void requestBetweenTimes() {
        String id = "USGS-10300000";

        ZonedDateTime beginTime = ZonedDateTime.of(1996, 12, 27, 6, 0, 0, 0, ZoneOffset.ofHours(0));
        ZonedDateTime endTime = ZonedDateTime.of(1997, 1, 15, 6, 0, 0, 0, ZoneOffset.ofHours(0));

        UsgsMonitoringLocation monitoringLocation = UsgsMonitoringLocation.from(id);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(monitoringLocation)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(beginTime)
                .setEndTime(endTime)
                .build();

        String response = request.retrieve();
        UsgsGageRecords usgsGageRecords = UsgsValuesParser.parse(response);
        UsgsGageRecord usgsGageRecord = usgsGageRecords
                .filter(monitoringLocation)
                .filter(UsgsParameter.DISCHARGE_CFS)
                .first();

        System.out.println(usgsGageRecord);
    }

    @Disabled("Live API smoke test")
    @Test
    void requestAfterBeginTime() {
        String id = "USGS-03034000";

        ZonedDateTime beginTime = ZonedDateTime.of(2025, 10, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));

        UsgsMonitoringLocation monitoringLocation = UsgsMonitoringLocation.from(id);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(monitoringLocation)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(beginTime)
                .build();

        String response = request.retrieve();
        UsgsGageRecords usgsGageRecords = UsgsValuesParser.parse(response);
        UsgsGageRecord usgsGageRecord = usgsGageRecords
                .filter(monitoringLocation)
                .filter(UsgsParameter.DISCHARGE_CFS)
                .first();

        System.out.println(usgsGageRecord);
    }

    @Disabled("Live API smoke test — pagination: exceeds 10000 feature limit")
    @Test
    void requestPaginatesOverLimit() {
        String[] ids = {
                "USGS-10297500", "USGS-10300000", "USGS-10308200", "USGS-10308800",
                "USGS-10309000", "USGS-10309035", "USGS-10309050", "USGS-10309070",
                "USGS-10309100", "USGS-10310000", "USGS-10310300", "USGS-10310350",
                "USGS-10310358", "USGS-10310402", "USGS-10310403", "USGS-10310448",
                "USGS-10310500", "USGS-10311000", "USGS-10311089", "USGS-10311090"
        };

        ZonedDateTime beginTime = ZonedDateTime.of(1996, 12, 27, 6, 0, 0, 0, ZoneOffset.ofHours(0));
        ZonedDateTime endTime = ZonedDateTime.of(1997, 1, 15, 6, 0, 0, 0, ZoneOffset.ofHours(0));

        UsgsValuesRequest.Builder builder = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(beginTime)
                .setEndTime(endTime);

        for (String id : ids) {
            builder.addMonitoringLocation(UsgsMonitoringLocation.from(id));
        }

        UsgsValuesRequest request = builder.build();
        String response = request.retrieve();
        UsgsGageRecords records = UsgsValuesParser.parse(response);

        System.out.println("Total records: " + records.records().size());
        for (UsgsGageRecord record : records.records()) {
            System.out.println(record.getMonitoringLocation().getMonitoringLocationId()
                    + " -> " + record.getTimes().length + " values");
        }
    }

    @Disabled("Live API smoke test")
    @Test
    void requestUtcVsMinusFourOffsetByFourHours() {
        String id = "USGS-03034000";
        UsgsMonitoringLocation location = UsgsMonitoringLocation.from(id);

        // Same wall-clock times, different zones: UTC and UTC-4
        ZonedDateTime beginUtc = ZonedDateTime.of(2018, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime endUtc = ZonedDateTime.of(2018, 9, 29, 0, 0, 0, 0, ZoneOffset.UTC);

        ZonedDateTime beginMinus4 = ZonedDateTime.of(2018, 9, 1, 0, 0, 0, 0, ZoneOffset.ofHours(-4));
        ZonedDateTime endMinus4 = ZonedDateTime.of(2018, 9, 29, 0, 0, 0, 0, ZoneOffset.ofHours(-4));

        // Request with UTC times
        UsgsValuesRequest utcRequest = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(location)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(beginUtc)
                .setEndTime(endUtc)
                .build();

        // Request with UTC-4 times (same wall clock, so actual instant is 4 hours later)
        UsgsValuesRequest minus4Request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(location)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(beginMinus4)
                .setEndTime(endMinus4)
                .build();

        UsgsGageRecord utcRecord = UsgsValuesParser.parse(utcRequest.retrieve())
                .filter(location)
                .filter(UsgsParameter.DISCHARGE_CFS)
                .first();

        UsgsGageRecord minus4Record = UsgsValuesParser.parse(minus4Request.retrieve())
                .filter(location)
                .filter(UsgsParameter.DISCHARGE_CFS)
                .first();

        assertTrue(utcRecord.size() > 0, "UTC record should have data");
        assertTrue(minus4Record.size() > 0, "UTC-4 record should have data");

        // The first timestamp from each request should differ by exactly 4 hours
        // because the same wall-clock in UTC-4 is 4 hours later in absolute time
        ZonedDateTime firstUtcTime = utcRecord.getTimes()[0];
        ZonedDateTime firstMinus4Time = minus4Record.getTimes()[0];
        Duration offset = Duration.between(firstUtcTime, firstMinus4Time);

        assertEquals(4, offset.toHours(),
                "First timestamps should be offset by 4 hours, but got " + offset);
    }

    @Test
    void chunksLongWindowByWaterYear() {
        ZonedDateTime begin = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = begin.plusDays(800);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(UsgsMonitoringLocation.from("USGS-03034000"))
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(begin)
                .setEndTime(end)
                .build();

        List<String> urls = request.buildRequestUrls();
        assertEquals(3, urls.size(), "800 days should split into stub + 1 year + partial");

        ZonedDateTime firstBoundary = ZonedDateTime.of(2018, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime secondBoundary = ZonedDateTime.of(2019, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        String firstBoundaryStr = firstBoundary.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String secondChunkBegin = firstBoundary.plusSeconds(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String secondChunkEnd = secondBoundary.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String thirdChunkBegin = secondBoundary.plusSeconds(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String endFormatted = end.withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        assertTrue(urls.get(0).contains("/" + firstBoundaryStr),
                "stub chunk should end at the next 1-October");
        assertTrue(urls.get(1).contains("=" + secondChunkBegin + "/" + secondChunkEnd),
                "second chunk should span a full water year 1s after prior boundary");
        assertTrue(urls.get(2).contains("=" + thirdChunkBegin + "/" + endFormatted),
                "last chunk should run from prior boundary to caller's endTime");
    }

    @Test
    void sixteenYearWindowChunksByYear() {
        ZonedDateTime begin = ZonedDateTime.of(1980, 12, 27, 6, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = ZonedDateTime.of(1997, 1, 15, 6, 0, 0, 0, ZoneOffset.UTC);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(UsgsMonitoringLocation.from("USGS-10336580"))
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(begin)
                .setEndTime(end)
                .build();

        List<String> urls = request.buildRequestUrls();
        long spanDays = Duration.between(begin, end).toDays();
        long expected = (spanDays + 364) / 365;
        assertEquals(expected, urls.size());
        for (String url : urls) {
            assertFalse(url.contains("1980-12-27T06:00:00Z/1997-01-15T06:00:00Z"),
                    "no chunk should span the full 16-year window");
        }
    }

    @Test
    void shortWindowUsesSingleUrl() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = begin.plusDays(30);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(UsgsMonitoringLocation.from("USGS-03034000"))
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(begin)
                .setEndTime(end)
                .build();

        assertEquals(1, request.buildRequestUrls().size());
    }

    @Test
    void fullWaterYearUsesSingleUrl() {
        ZonedDateTime begin = ZonedDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = ZonedDateTime.of(2024, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(UsgsMonitoringLocation.from("USGS-03034000"))
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(begin)
                .setEndTime(end)
                .build();

        assertEquals(1, request.buildRequestUrls().size());
    }

    @Test
    void yearWindowCrossingOctoberSplitsAtWaterYear() {
        ZonedDateTime begin = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = begin.plusDays(365);

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(UsgsMonitoringLocation.from("USGS-03034000"))
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(begin)
                .setEndTime(end)
                .build();

        assertEquals(2, request.buildRequestUrls().size(),
                "a window that crosses 1-October should split into a stub and a remainder");
    }

    @Test
    void durationRequestUsesSingleUrl() {
        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(UsgsMonitoringLocation.from("USGS-03034000"))
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setDuration(Duration.ofDays(4000))
                .build();

        assertEquals(1, request.buildRequestUrls().size());
    }

    @Test
    void openEndedBeginOnlyUsesSingleUrl() {
        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocation(UsgsMonitoringLocation.from("USGS-03034000"))
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(ZonedDateTime.of(2010, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .build();

        assertEquals(1, request.buildRequestUrls().size());
    }

    @Disabled("Live API smoke test")
    @Test
    void requestMultipleLocations() {
        UsgsMonitoringLocation location1 = UsgsMonitoringLocation.from("USGS-03034000");
        UsgsMonitoringLocation location2 = UsgsMonitoringLocation.from("USGS-01541000");

        List<UsgsMonitoringLocation> monitoringLocations = new ArrayList<>();
        monitoringLocations.add(location1);
        monitoringLocations.add(location2);

        ZonedDateTime beginTime = ZonedDateTime.of(2025, 10, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.CONTINUOUS)
                .addMonitoringLocations(monitoringLocations)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.INSTANTANEOUS)
                .setBeginTime(beginTime)
                .build();

        String response = request.retrieve();
        UsgsGageRecords usgsGageRecords = UsgsValuesParser.parse(response);
        UsgsGageRecord usgsGageRecord = usgsGageRecords
                .filter(location1)
                .filter(UsgsParameter.DISCHARGE_CFS)
                .first();

        System.out.println(usgsGageRecord);
    }
}
