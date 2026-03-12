package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

class UsgsDailyValuesRequestTest {

    @Disabled("Live API smoke test")
    @Test
    void requestBetweenTimes() {
        UsgsMonitoringLocation monitoringLocation = UsgsMonitoringLocation.from("USGS-03034000");

        ZonedDateTime beginTime = ZonedDateTime.of(1951, 10, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));
        ZonedDateTime endTime = ZonedDateTime.of(1951, 11, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.DAILY)
                .addMonitoringLocation(monitoringLocation)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.MEAN)
                .setBeginTime(beginTime)
                .setEndTime(endTime)
                .build();

        String response = request.retrieve();
        UsgsGageRecords usgsGageRecords = UsgsValuesParser.parse(response);
        UsgsGageRecord usgsGageRecord = usgsGageRecords.filter(monitoringLocation).filter(UsgsParameter.DISCHARGE_CFS).first();

        System.out.println(usgsGageRecord);
    }

    @Disabled("Live API smoke test")
    @Test
    void requestAfterBeginTime() {
        UsgsMonitoringLocation monitoringLocation = UsgsMonitoringLocation.from("USGS-03034000");

        ZonedDateTime beginTime = ZonedDateTime.of(2025, 10, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.DAILY)
                .addMonitoringLocation(monitoringLocation)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.MEAN)
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

    @Disabled("Live API smoke test")
    @Test
    void requestBeforeEndTime() {
        UsgsMonitoringLocation monitoringLocation = UsgsMonitoringLocation.from("USGS-03034000");

        ZonedDateTime endTime = ZonedDateTime.of(1951, 11, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));

        UsgsValuesRequest request = UsgsValuesRequest.builder()
                .setService(UsgsService.DAILY)
                .addMonitoringLocation(monitoringLocation)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.MEAN)
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
}
