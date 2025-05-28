package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsgsSiteRequestTest {

    @Test
    void siteRequest() {
        ZonedDateTime startTime = ZonedDateTime.of(1951, 10, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime endTime = ZonedDateTime.of(1951, 11, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

        UsgsTemporalRequest temporalRequest = UsgsDailyRequest.of(startTime, endTime);

        UsgsRequest request = UsgsSiteRequest.builder()
                .addParameter(UsgsParameter.DISCHARGE_CFS)
                .setBoundingBox(41.0845925, 40.8611339, -78.6874107, -79.0344531)
                .setTemporalRequest(temporalRequest)
                .build();

        String response = request.retrieve();
        List<UsgsSite> usgsSites = UsgsSiteParser.parse(response);
        assertEquals(1, usgsSites.size());
    }
}