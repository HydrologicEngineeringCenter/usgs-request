package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class UsgsValuesRequest extends UsgsRequest {
    private static final String FORMAT_JSON = "f=json";
    private static final String LIMIT_50000 = "&limit=50000";

    private final List<UsgsMonitoringLocation> monitoringLocations;
    private final UsgsService service;

    private final UsgsParameter parameter;
    private final UsgsStatisticId statisticId;

    private final ZonedDateTime beginTime;
    private final ZonedDateTime endTime;
    private final Duration duration;

    UsgsValuesRequest(Builder builder) {
        super(builder.apiKey);
        monitoringLocations = List.copyOf(builder.monitoringLocations);
        service = builder.service;

        parameter = builder.parameter;
        statisticId = builder.statisticId;

        beginTime = builder.beginTime;
        endTime = builder.endTime;
        duration = builder.duration;
    }

    public static class Builder {
        private final List<UsgsMonitoringLocation> monitoringLocations = new ArrayList<>();

        private UsgsService service;

        private UsgsParameter parameter;
        private UsgsStatisticId statisticId;

        private ZonedDateTime beginTime;
        private ZonedDateTime endTime;
        private Duration duration;
        private String apiKey;

        public Builder addMonitoringLocation(UsgsMonitoringLocation location) {
            this.monitoringLocations.add(location);
            return this;
        }

        public Builder addMonitoringLocations(List<UsgsMonitoringLocation> locations) {
            this.monitoringLocations.addAll(locations);
            return this;
        }

        public Builder setService(UsgsService service) {
            this.service = service;
            return this;
        }

        public Builder setParameter(UsgsParameter parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder setStatisticType(UsgsStatisticId statisticId) {
            this.statisticId = statisticId;
            return this;
        }

        public Builder setBeginTime(ZonedDateTime beginTime) {
            this.beginTime = beginTime;
            return this;
        }

        public Builder setEndTime(ZonedDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder setDuration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        void validate() {
            // Validate service
            if (service == null)
                throw new IllegalStateException("Service cannot be null or empty");

            // Validate monitoring locations
            if (monitoringLocations.isEmpty()) {
                throw new IllegalStateException("At least one monitoring location must be provided");
            }

            // Validate parameter
            if (parameter == null)
                throw new IllegalStateException("Parameter must be provided");

            // Validate that duration is positive
            if (duration != null && (duration.isZero() || duration.isNegative())) {
                throw new IllegalStateException("Duration must be positive");
            }

            // Validate that duration and begin/end times are not both set
            if (duration != null && (beginTime != null || endTime != null)) {
                throw new IllegalStateException("Duration cannot be used together with begin/end time");
            }

            // Validate that end is after beginTime
            if (beginTime != null && endTime != null && (endTime.isBefore(beginTime) || endTime.isEqual(beginTime))) {
                throw new IllegalStateException("End datetime must be after beginTime datetime");
            }
        }

        public UsgsValuesRequest build() {
            validate();

            return switch (service) {
                case DAILY -> new UsgsDailyValuesRequest(this);
                case CONTINUOUS -> new UsgsContinuousValuesRequest(this);
            };
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return getServiceUrl() +
                FORMAT_JSON +
                formatIds() +
                formatParameterCode() +
                formatStatisticId() +
                formatTime() +
                LIMIT_50000 +
                formatApiKey();
    }

    String buildUrlForWindow(ZonedDateTime windowBegin, ZonedDateTime windowEnd) {
        return getServiceUrl() +
                FORMAT_JSON +
                formatIds() +
                formatParameterCode() +
                formatStatisticId() +
                "&time=" + toUtcString(windowBegin) + "/" + toUtcString(windowEnd) +
                LIMIT_50000 +
                formatApiKey();
    }

    ZonedDateTime getBeginTime() {
        return beginTime;
    }

    ZonedDateTime getEndTime() {
        return endTime;
    }

    public List<UsgsMonitoringLocation> getMonitoringLocations() {
        return monitoringLocations;
    }

    public UsgsService getService() {
        return service;
    }

    public UsgsParameter getParameter() {
        return parameter;
    }

    /**
     * Split a time range into water-year-aligned chunks. The first chunk ends at the next
     * 1-October boundary (if one falls before {@code end}); subsequent chunks span a full
     * calendar year each. USGS records often start and stop on water-year boundaries, so
     * aligning chunk edges there reduces single-sample boundary artifacts.
     *
     * @param begin inclusive start of the overall window
     * @param end   inclusive end of the overall window
     * @return ordered list of {@code [chunkBegin, chunkEnd]} pairs covering {@code [begin, end]}
     */
    public static List<ZonedDateTime[]> splitByWaterYear(ZonedDateTime begin, ZonedDateTime end) {
        List<ZonedDateTime[]> chunks = new ArrayList<>();
        ZonedDateTime current = begin;

        ZonedDateTime nextOct1 = nextWaterYearStart(current);
        if (nextOct1.isAfter(current) && nextOct1.isBefore(end)) {
            chunks.add(new ZonedDateTime[]{current, nextOct1});
            current = nextOct1;
        }

        while (current.isBefore(end)) {
            ZonedDateTime next = current.plusYears(1);
            if (next.isAfter(end)) {
                next = end;
            }
            chunks.add(new ZonedDateTime[]{current, next});
            current = next;
        }
        return chunks;
    }

    private static ZonedDateTime nextWaterYearStart(ZonedDateTime t) {
        ZonedDateTime thisOct1 = t.withMonth(10).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return t.isBefore(thisOct1) ? thisOct1 : thisOct1.plusYears(1);
    }

    abstract String getServiceUrl();

    private String formatIds() {
        if (monitoringLocations.isEmpty())
            return "";

        String ids = monitoringLocations.stream()
                .map(UsgsMonitoringLocation::getMonitoringLocationId)
                .collect(Collectors.joining(","));
        return "&monitoring_location_id=" + ids;
    }

    private String formatParameterCode() {
        if (parameter == null)
            return "";

        return "&parameter_code=" + parameter.getCode();
    }

    private String formatStatisticId() {
        if (statisticId == null)
            return "";

        return "&statistic_id=" + statisticId.getCode();
    }

    private String formatTime() {
        if (duration != null)
            return "&time=" + duration;

        if (beginTime != null && endTime != null)
            return "&time=" + toUtcString(beginTime) + "/" + toUtcString(endTime);

        if (beginTime != null)
            return "&time=" + toUtcString(beginTime) + "/..";

        if (endTime != null)
            return "&time=../" + toUtcString(endTime);

        return "";
    }

    private static String toUtcString(ZonedDateTime time) {
        return time.withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
