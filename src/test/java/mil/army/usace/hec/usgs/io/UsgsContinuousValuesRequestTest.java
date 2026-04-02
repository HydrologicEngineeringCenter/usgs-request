package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
