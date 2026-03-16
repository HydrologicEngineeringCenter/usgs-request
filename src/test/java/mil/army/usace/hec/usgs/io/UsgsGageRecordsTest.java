package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsgsGageRecordsTest {

    private static UsgsMonitoringLocation location1() {
        return UsgsMonitoringLocation.from("USGS-03034000");
    }

    private static UsgsMonitoringLocation location2() {
        return UsgsMonitoringLocation.from("USGS-01541000");
    }

    private static UsgsGageRecord createRecord(UsgsMonitoringLocation location, UsgsParameter parameter) {
        ZonedDateTime t1 = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        return UsgsGageRecord.builder()
                .setMonitoringLocation(location)
                .setParameter(parameter)
                .setTimes(new ZonedDateTime[]{t1})
                .setValues(new double[]{100.0})
                .setNoDataValue(Double.NaN)
                .build();
    }

    @Test
    void filterByMonitoringLocation() {
        UsgsGageRecord record1 = createRecord(location1(), UsgsParameter.DISCHARGE_CFS);
        UsgsGageRecord record2 = createRecord(location2(), UsgsParameter.DISCHARGE_CFS);

        UsgsGageRecords records = UsgsGageRecords.from(List.of(record1, record2));
        UsgsGageRecords filtered = records.filter(location1());

        assertEquals(1, filtered.count());
        assertEquals(location1(), filtered.first().getMonitoringLocation());
    }

    @Test
    void filterByParameter() {
        UsgsGageRecord discharge = createRecord(location1(), UsgsParameter.DISCHARGE_CFS);
        UsgsGageRecord stage = createRecord(location1(), UsgsParameter.STAGE_FT);

        UsgsGageRecords records = UsgsGageRecords.from(List.of(discharge, stage));
        UsgsGageRecords filtered = records.filter(UsgsParameter.STAGE_FT);

        assertEquals(1, filtered.count());
        assertEquals(UsgsParameter.STAGE_FT, filtered.first().getParameter());
    }

    @Test
    void filterChaining() {
        UsgsGageRecord r1 = createRecord(location1(), UsgsParameter.DISCHARGE_CFS);
        UsgsGageRecord r2 = createRecord(location1(), UsgsParameter.STAGE_FT);
        UsgsGageRecord r3 = createRecord(location2(), UsgsParameter.DISCHARGE_CFS);

        UsgsGageRecords records = UsgsGageRecords.from(List.of(r1, r2, r3));
        UsgsGageRecord result = records
                .filter(location1())
                .filter(UsgsParameter.DISCHARGE_CFS)
                .first();

        assertEquals(location1(), result.getMonitoringLocation());
        assertEquals(UsgsParameter.DISCHARGE_CFS, result.getParameter());
    }

    @Test
    void firstOnEmptyReturnsEmptyRecord() {
        UsgsGageRecords records = UsgsGageRecords.from(List.of());
        UsgsGageRecord result = records.first();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void filterNoMatch() {
        UsgsGageRecord record = createRecord(location1(), UsgsParameter.DISCHARGE_CFS);
        UsgsGageRecords records = UsgsGageRecords.from(List.of(record));

        UsgsGageRecords filtered = records.filter(location2());
        assertEquals(0, filtered.count());
    }

    @Test
    void monitoringLocationIds() {
        UsgsGageRecord r1 = createRecord(location1(), UsgsParameter.DISCHARGE_CFS);
        UsgsGageRecord r2 = createRecord(location2(), UsgsParameter.DISCHARGE_CFS);
        UsgsGageRecord r3 = createRecord(location1(), UsgsParameter.STAGE_FT);

        UsgsGageRecords records = UsgsGageRecords.from(List.of(r1, r2, r3));
        List<String> ids = records.monitoringLocationIds();

        assertEquals(2, ids.size());
        assertTrue(ids.contains("USGS-03034000"));
        assertTrue(ids.contains("USGS-01541000"));
    }

    @Test
    void count() {
        UsgsGageRecord r1 = createRecord(location1(), UsgsParameter.DISCHARGE_CFS);
        UsgsGageRecord r2 = createRecord(location2(), UsgsParameter.DISCHARGE_CFS);

        UsgsGageRecords records = UsgsGageRecords.from(List.of(r1, r2));
        assertEquals(2, records.count());
    }
}
