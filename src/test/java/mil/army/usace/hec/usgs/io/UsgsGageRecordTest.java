package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsgsGageRecordTest {

    private static UsgsMonitoringLocation testLocation() {
        return UsgsMonitoringLocation.from("USGS-03034000");
    }

    @Test
    void buildWithValidInputs() {
        ZonedDateTime t1 = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime t2 = ZonedDateTime.of(2024, 1, 1, 0, 15, 0, 0, ZoneOffset.UTC);

        UsgsGageRecord record = UsgsGageRecord.builder()
                .setMonitoringLocation(testLocation())
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTimes(new ZonedDateTime[]{t1, t2})
                .setValues(new double[]{100.0, 200.0})
                .setNoDataValue(Double.NaN)
                .build();

        assertEquals(2, record.size());
        assertEquals(testLocation(), record.getMonitoringLocation());
        assertEquals(UsgsParameter.DISCHARGE_CFS, record.getParameter());
    }

    @Test
    void buildThrowsWithoutMonitoringLocation() {
        assertThrows(IllegalStateException.class, () ->
                UsgsGageRecord.builder()
                        .setParameter(UsgsParameter.DISCHARGE_CFS)
                        .setTimes(new ZonedDateTime[]{})
                        .setValues(new double[]{})
                        .build());
    }

    @Test
    void buildThrowsWithoutParameter() {
        assertThrows(IllegalStateException.class, () ->
                UsgsGageRecord.builder()
                        .setMonitoringLocation(testLocation())
                        .setTimes(new ZonedDateTime[]{})
                        .setValues(new double[]{})
                        .build());
    }

    @Test
    void buildThrowsMismatchedArrayLengths() {
        ZonedDateTime t1 = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        assertThrows(IllegalStateException.class, () ->
                UsgsGageRecord.builder()
                        .setMonitoringLocation(testLocation())
                        .setParameter(UsgsParameter.DISCHARGE_CFS)
                        .setTimes(new ZonedDateTime[]{t1})
                        .setValues(new double[]{1.0, 2.0})
                        .build());
    }

    @Test
    void emptyRecord() {
        UsgsGageRecord record = UsgsGageRecord.empty();
        assertEquals(0, record.size());
        assertNotNull(record.getMonitoringLocation());
        assertEquals(UsgsParameter.UNKNOWN, record.getParameter());
    }

    @Test
    void singleDataPoint() {
        ZonedDateTime t1 = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        UsgsGageRecord record = UsgsGageRecord.builder()
                .setMonitoringLocation(testLocation())
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTimes(new ZonedDateTime[]{t1})
                .setValues(new double[]{42.0})
                .setNoDataValue(Double.NaN)
                .build();

        assertEquals(1, record.size());
        assertEquals(42.0, record.getValues()[0]);
        assertEquals(0, record.getIntervalSeconds());
    }

    @Test
    void normalizationFillsGaps() {
        ZonedDateTime t1 = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime t2 = ZonedDateTime.of(2024, 1, 1, 0, 15, 0, 0, ZoneOffset.UTC);
        // Gap at 0:30
        ZonedDateTime t3 = ZonedDateTime.of(2024, 1, 1, 0, 45, 0, 0, ZoneOffset.UTC);

        UsgsGageRecord record = UsgsGageRecord.builder()
                .setMonitoringLocation(testLocation())
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTimes(new ZonedDateTime[]{t1, t2, t3})
                .setValues(new double[]{100.0, 200.0, 400.0})
                .setNoDataValue(-999.0)
                .build();

        // Should have 4 entries: 0:00, 0:15, 0:30 (gap filled), 0:45
        assertEquals(4, record.size());
        assertEquals(900, record.getIntervalSeconds()); // 15 minutes
        assertEquals(-999.0, record.getValues()[2]); // gap filled with noDataValue
    }

    @Test
    void defensiveCopyOnGetTimes() {
        ZonedDateTime t1 = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime t2 = ZonedDateTime.of(2024, 1, 1, 0, 15, 0, 0, ZoneOffset.UTC);

        UsgsGageRecord record = UsgsGageRecord.builder()
                .setMonitoringLocation(testLocation())
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTimes(new ZonedDateTime[]{t1, t2})
                .setValues(new double[]{1.0, 2.0})
                .setNoDataValue(Double.NaN)
                .build();

        ZonedDateTime[] times = record.getTimes();
        times[0] = null; // mutate the copy
        assertNotNull(record.getTimes()[0]); // original is unchanged
    }

    @Test
    void defensiveCopyOnGetValues() {
        ZonedDateTime t1 = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime t2 = ZonedDateTime.of(2024, 1, 1, 0, 15, 0, 0, ZoneOffset.UTC);

        UsgsGageRecord record = UsgsGageRecord.builder()
                .setMonitoringLocation(testLocation())
                .setParameter(UsgsParameter.DISCHARGE_CFS)
                .setTimes(new ZonedDateTime[]{t1, t2})
                .setValues(new double[]{1.0, 2.0})
                .setNoDataValue(Double.NaN)
                .build();

        double[] values = record.getValues();
        values[0] = -1.0; // mutate the copy
        assertEquals(1.0, record.getValues()[0]); // original is unchanged
    }
}
