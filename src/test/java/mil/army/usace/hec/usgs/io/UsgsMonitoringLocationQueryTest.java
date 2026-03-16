package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsgsMonitoringLocationQueryTest {

    @Test
    void buildWithMandatoryFieldsOnly() {
        UsgsMonitoringLocationQuery query = UsgsMonitoringLocationQuery.builder()
                .setBoundingBox(41.0845925, 40.8611339, -78.6874107, -79.0344531)
                .build();

        assertNotNull(query);
    }

    @Test
    void buildWithAllFields() {
        ZonedDateTime startTime = ZonedDateTime.of(1951, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime endTime = ZonedDateTime.of(1951, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsMonitoringLocationQuery query = UsgsMonitoringLocationQuery.builder()
                .setBoundingBox(41.0845925, 40.8611339, -78.6874107, -79.0344531)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.MEAN)
                .setBeginTime(startTime)
                .setEndTime(endTime)
                .build();

        assertNotNull(query);
    }

    @Test
    void buildWithoutBoundingBoxThrows() {
        assertThrows(IllegalStateException.class, () ->
                UsgsMonitoringLocationQuery.builder().build());
    }

    @Disabled("Live API smoke test")
    @Test
    void siteRequest() {
        ZonedDateTime startTime = ZonedDateTime.of(1951, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime endTime = ZonedDateTime.of(1951, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsMonitoringLocationQuery query = UsgsMonitoringLocationQuery.builder()
                .setBoundingBox(41.0845925, 40.8611339, -78.6874107, -79.0344531)
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setStatisticType(UsgsStatisticId.MEAN)
                .setBeginTime(startTime)
                .setEndTime(endTime)
                .build();

        List<UsgsMonitoringLocation> result = query.call();

        assertEquals(1, result.size());
    }
}
