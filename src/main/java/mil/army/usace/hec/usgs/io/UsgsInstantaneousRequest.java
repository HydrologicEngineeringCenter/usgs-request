package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UsgsInstantaneousRequest implements UsgsTemporalRequest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    private Duration duration = Duration.ZERO;

    private UsgsInstantaneousRequest(ZonedDateTime startTime, ZonedDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private UsgsInstantaneousRequest(Duration duration) {
        this.duration = duration;
    }

    public static UsgsTemporalRequest of(ZonedDateTime startTime, ZonedDateTime endTime) {
        return new UsgsInstantaneousRequest(startTime, endTime);
    }

    public static UsgsTemporalRequest of(Duration duration) {
        return new UsgsInstantaneousRequest(duration);
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
        return duration;
    }

    @Override
    public UsgsDataType getDataType() {
        return UsgsDataType.INSTANTANEOUS;
    }

    @Override
    public DateTimeFormatter dateTimeFormatter() {
        return FORMATTER;
    }
}
