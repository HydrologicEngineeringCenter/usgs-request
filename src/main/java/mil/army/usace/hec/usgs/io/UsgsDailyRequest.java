package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UsgsDailyRequest implements UsgsTemporalRequest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;

    private UsgsDailyRequest(ZonedDateTime startTime, ZonedDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static UsgsDailyRequest of(ZonedDateTime startTime, ZonedDateTime endTime) {
        if (endTime.isBefore(startTime))
            throw new IllegalArgumentException("End time must be after start time");

        return new UsgsDailyRequest(startTime, endTime);
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime == null ? null : startTime.toLocalDateTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime == null ? null : endTime.toLocalDateTime();
    }

    @Override
    public Duration getPeriod() {
        return Duration.ZERO;
    }

    @Override
    public UsgsDataType getDataType() {
        return UsgsDataType.DAILY;
    }

    @Override
    public DateTimeFormatter dateTimeFormatter() {
        return FORMATTER;
    }
}
