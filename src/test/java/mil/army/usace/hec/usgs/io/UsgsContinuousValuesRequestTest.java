package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

class UsgsContinuousValuesRequestTest {

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
