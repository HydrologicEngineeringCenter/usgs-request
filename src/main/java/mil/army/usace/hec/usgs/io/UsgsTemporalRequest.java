package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public interface UsgsTemporalRequest {

    static UsgsTemporalRequest of(UsgsDataType dataType, ZonedDateTime startTime, ZonedDateTime endTime) {
        if (dataType == UsgsDataType.DAILY)
            return UsgsDailyRequest.of(startTime, endTime);
        if (dataType == UsgsDataType.INSTANTANEOUS)
            return UsgsInstantaneousRequest.of(startTime, endTime);
        throw new IllegalArgumentException("UsgsDataType \"" + dataType + "\" not recognized");
    }

    LocalDateTime getStartTime();

    LocalDateTime getEndTime();

    Duration getPeriod();

    UsgsDataType getDataType();

    DateTimeFormatter dateTimeFormatter();
}
