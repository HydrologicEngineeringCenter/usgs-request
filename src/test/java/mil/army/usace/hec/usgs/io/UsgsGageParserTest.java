package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

class UsgsGageParserTest {

    @Test
    void parseInstantaneous() {
        URL inUrl = Objects.requireNonNull(getClass().getResource(
                "/usgs_instantaneous.json"));

        Path path = new File(inUrl.getFile()).toPath();

        try {
            String response = Files.readString(path);
            UsgsGageRecords usgsGageRecords = UsgsGageParser.parse(response);
            UsgsGageRecord usgsGageRecord = usgsGageRecords.get("03034000");
            Assertions.assertEquals(11, usgsGageRecord.size());
        } catch (IOException e) {
            Assertions.fail();
        }
    }

    @Test
    void parseDaily() {
        URL inUrl = Objects.requireNonNull(getClass().getResource(
                "/usgs_daily.json"));

        Path path = new File(inUrl.getFile()).toPath();

        try {
            String response = Files.readString(path);
            UsgsGageRecords usgsGageRecords = UsgsGageParser.parse(response);
            UsgsGageRecord usgsGageRecord = usgsGageRecords.get("03034000");
            Assertions.assertEquals(32, usgsGageRecord.size());
        } catch (IOException e) {
            Assertions.fail();
        }
    }

    @Test
    void parseDailyTruckee() {
        URL inUrl = Objects.requireNonNull(getClass().getResource(
                "/usgs_daily_truckee.json"));

        Path path = new File(inUrl.getFile()).toPath();

        try {
            String response = Files.readString(path);
            UsgsGageRecords usgsGageRecords = UsgsGageParser.parse(response);
            Assertions.assertEquals(115, usgsGageRecords.count());
        } catch (IOException e) {
            Assertions.fail();
        }
    }

    @Test
    void parseInstWolf() {
        URL inUrl = Objects.requireNonNull(getClass().getResource(
                "/usgs_inst_wolf.json"));

        Path path = new File(inUrl.getFile()).toPath();

        try {
            String response = Files.readString(path);
            UsgsGageRecords usgsGageRecords = UsgsGageParser.parse(response);
            UsgsGageRecord usgsGageRecord = usgsGageRecords.get("07030392");
            double[] values = usgsGageRecord.getValues();
            double noDataValue = usgsGageRecord.getNoDataValue();
            int validCount = 0;
            for (double value : values) {
                if (Double.compare(value, noDataValue) != 0) {
                    validCount++;
                }
            }
            // Assert valid value count. This was incorrect in previous code.
            Assertions.assertEquals(6518, validCount);
        } catch (IOException e) {
            Assertions.fail();
        }
    }
}