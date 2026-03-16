package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

class UsgsValuesParserTest {

    /*
     * Criteria:
     * Service: continuous (instantaneous values)
     * Monitoring location ID: USGS-03034000
     * Parameter: Discharge (00060)
     * Period: 3 hours (2-Sep-2022 17:15 to 19:45 EDT)
     */
    @Test
    void parseInstantaneous() throws IOException {
        URL inUrl = Objects.requireNonNull(getClass().getResource(
                "/usgs_continuous_mahoning.json"));

        Path path = new File(inUrl.getFile()).toPath();

        String response = Files.readString(path);
        UsgsGageRecords usgsGageRecords = UsgsValuesParser.parse(response);
        UsgsGageRecord usgsGageRecord = usgsGageRecords
                .filter(UsgsMonitoringLocation.from("USGS-03034000"))
                .filter(UsgsParameter.DISCHARGE_CFS)
                .first();
        Assertions.assertEquals(10, usgsGageRecord.size());
    }

    /*
     * Criteria:
     * Service: daily
     * Monitoring location ID: USGS-03034000
     * Begin time: 1-Oct-1951
     * End time: 31-Oct-1951
     */
    @Test
    void parseDaily() throws IOException {
        URL inUrl = Objects.requireNonNull(getClass().getResource(
                "/usgs_daily_mahoning.json"));

        Path path = new File(inUrl.getFile()).toPath();

        String response = Files.readString(path);
        UsgsGageRecords usgsGageRecords = UsgsValuesParser.parse(response);
        UsgsGageRecord usgsGageRecord = usgsGageRecords
                .filter(UsgsMonitoringLocation.from("USGS-03034000"))
                .filter(UsgsParameter.DISCHARGE_CFS)
                .first();
        Assertions.assertEquals(31, usgsGageRecord.size());
    }

    /*
     * Criteria:
     * Service: daily
     * Monitoring locations: 115 sites queried in the Truckee River basin (9 had no data for this period)
     * Parameter: Discharge (00060)
     * Begin time: 27-Dec-1996
     * End time: 15-Jan-1997
     */
    @Test
    void parseDailyTruckee() throws IOException {
        URL inUrl = Objects.requireNonNull(getClass().getResource(
                "/usgs_daily_truckee.json"));

        Path path = new File(inUrl.getFile()).toPath();

        String response = Files.readString(path);
        UsgsGageRecords usgsGageRecords = UsgsValuesParser.parse(response);
        Assertions.assertEquals(106, usgsGageRecords.count());
    }
}
