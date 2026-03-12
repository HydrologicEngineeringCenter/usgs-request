package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class UsgsValuesRequest extends UsgsRequest {
    private static final String FORMAT_JSON = "f=json";
    private static final String LIMIT_10000 = "&limit=10000";

    private final List<UsgsMonitoringLocation> monitoringLocations;
    private final UsgsService service;

    private final UsgsParameter parameter;
    private final UsgsStatisticId statisticId;

    private final ZonedDateTime beginTime;
    private final ZonedDateTime endTime;
    private final Duration duration;

    UsgsValuesRequest(Builder builder) {
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

        void validate() {
            // Validate service
            if (service == null)
                throw new IllegalStateException("Service cannot be null or empty");

            // Validate monitoring locations
            if (monitoringLocations.isEmpty()) {
                throw new IllegalStateException("At least one monitoring location must be provided");
            }

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
                LIMIT_10000;
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
            return "&time=" + beginTime + "/" + endTime;

        if (beginTime != null)
            return "&time=" + beginTime + "/..";

        if (endTime != null)
            return "&time=../" + endTime;

        return "";
    }
}
