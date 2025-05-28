package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

class UsgsDataRequestTest {

    @Test
    void retrievePrevious3Hours() {
        List<UsgsSite> sites = new ArrayList<>();
        sites.add(UsgsSite.from("03034000"));

        UsgsTemporalRequest temporalRequest = UsgsInstantaneousRequest.of(Duration.ofHours(3));

        UsgsRequest request = UsgsDataRequest.builder()
                .setSites(sites)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTemporalRequest(temporalRequest)
                .build();

        String response = request.retrieve();
        UsgsGageRecords usgsGageRecords = UsgsGageParser.parse(response);
        Assertions.assertEquals(1, usgsGageRecords.count());
    }

    @Test
    void retrieveCurrent() {
        List<UsgsSite> sites = new ArrayList<>();
        sites.add(UsgsSite.from("03034000"));

        ZonedDateTime endTime = ZonedDateTime.now();
        ZonedDateTime startTime = endTime.minusDays(2);

        UsgsTemporalRequest temporalRequest = UsgsInstantaneousRequest.of(startTime, endTime);

        UsgsRequest request = UsgsDataRequest.builder()
                .setSites(sites)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTemporalRequest(temporalRequest)
                .build();

        String response = request.retrieve();
        UsgsGageRecords usgsGageRecords = UsgsGageParser.parse(response);
        Assertions.assertEquals(1, usgsGageRecords.count());
    }

    @Test
    void retrieveDST() {
        List<UsgsSite> sites = new ArrayList<>();
        sites.add(UsgsSite.from("03034000"));

        ZonedDateTime startTime = ZonedDateTime.of(LocalDateTime.of(2024, 11, 3, 1, 30, 0, 0), ZoneId.of("UTC-04:00"));
        ZonedDateTime endTime = ZonedDateTime.of(LocalDateTime.of(2024, 11, 3, 2, 0, 0, 0), ZoneId.of("UTC-04:00"));

        UsgsTemporalRequest temporalRequest = UsgsInstantaneousRequest.of(startTime, endTime);

        UsgsRequest request = UsgsDataRequest.builder()
                .setSites(sites)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTemporalRequest(temporalRequest)
                .build();

        String response = request.retrieve();
        UsgsGageRecords usgsGageRecords = UsgsGageParser.parse(response);
        Assertions.assertEquals(1, usgsGageRecords.count());
    }

    @Test
    void retrieveDaily() {
        List<UsgsSite> sites = new ArrayList<>();
        sites.add(UsgsSite.from("03034000"));

        ZonedDateTime startTime = ZonedDateTime.of(1951, 10, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime endTime = ZonedDateTime.of(1951, 11, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

        UsgsTemporalRequest temporalRequest = UsgsDailyRequest.of(startTime, endTime);

        UsgsRequest request = UsgsDataRequest.builder()
                .setSites(sites)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTemporalRequest(temporalRequest)
                .build();

        String response = request.retrieve();
        UsgsGageRecords usgsGageRecords = UsgsGageParser.parse(response);
        Assertions.assertEquals(1, usgsGageRecords.count());
    }
}