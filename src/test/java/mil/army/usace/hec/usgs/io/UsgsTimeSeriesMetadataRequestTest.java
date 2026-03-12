package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsgsTimeSeriesMetadataRequestTest {

    @Disabled("Live API smoke test")
    @Test
    void requestTest() {
        ZonedDateTime startTime = ZonedDateTime.of(1951, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime endTime = ZonedDateTime.of(1951, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsRequest request = UsgsTimeSeriesMetadataRequest.builder()
                .setBoundingBox(41.0845925, 40.8611339, -78.6874107, -79.0344531)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.MEAN)
                .setBeginTime(startTime)
                .setEndTime(endTime)
                .build();

        String response = request.retrieve();
        List<UsgsTimeSeriesMetadata> usgsTimeSeriesMetadata = UsgsTimeSeriesMetadataParser.parse(response);
        assertEquals(1, usgsTimeSeriesMetadata.size());
    }
}
