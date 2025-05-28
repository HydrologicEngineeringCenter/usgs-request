package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UsgsDataRequest extends UsgsRequest {
    private static final String USGS_DAILY_VALUE_PROP = "usgs-daily-values-service-url";
    private static final String USGS_INST_VALUE_PROP = "usgs-instantaneous-values-service-url";
    private static final String USGS_DAILY_VALUE_URL = "usgs-daily-values-service=https://waterservices.usgs.gov/nwis/dv/?";
    private static final String USGS_INST_VALUE_URL = "usgs-instantaneous-values-service=https://waterservices.usgs.gov/nwis/iv/?";

    private static final String FORMAT_JSON = "&format=json";

    private final List<UsgsSite> sites;
    private final UsgsParameter parameter;
    private final UsgsTemporalRequest temporalRequest;

    UsgsDataRequest(Builder builder) {
        sites = builder.sites;
        parameter = builder.parameter;
        temporalRequest = builder.temporalRequest;
    }

    public static class Builder {
        private final List<UsgsSite> sites = new ArrayList<>();
        private UsgsParameter parameter;
        private UsgsTemporalRequest temporalRequest;

        public Builder setSites(List<UsgsSite> sites) {
            this.sites.clear();
            this.sites.addAll(new HashSet<>(sites));
            return this;
        }

        public Builder setParameter(UsgsParameter parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder setTemporalRequest(UsgsTemporalRequest temporalRequest) {
            this.temporalRequest = temporalRequest;
            return this;
        }

        public UsgsDataRequest build() {
            if (sites.isEmpty())
                throw new IllegalArgumentException("At least one site must be provided");
            if (parameter == null)
                throw new IllegalArgumentException("Parameter code must be provided");
            if (temporalRequest == null)
                throw new IllegalArgumentException("Temporal request must be provided");
            return new UsgsDataRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        UsgsDataType dataType = temporalRequest.getDataType();

        String url = dataType == UsgsDataType.DAILY ? getUsgsDailyServiceUrl() : getUsgsInstServiceUrl();

        return url +
                formatSites() +
                formatParameter() +
                formatTime() +
                FORMAT_JSON;
    }

    public List<UsgsSite> getSites() {
        return List.copyOf(sites);
    }

    public UsgsTemporalRequest getTemporalRequest() {
        return temporalRequest;
    }

    public UsgsParameter getParameter() {
        return parameter;
    }

    private static String getUsgsDailyServiceUrl() {
        String urlString = Config.INSTANCE.getOrDefault(USGS_DAILY_VALUE_PROP, USGS_DAILY_VALUE_URL);
        return UrlValidator.validate(urlString);
    }

    private static String getUsgsInstServiceUrl() {
        String urlString = Config.INSTANCE.getOrDefault(USGS_INST_VALUE_PROP, USGS_INST_VALUE_URL);
        return UrlValidator.validate(urlString);
    }

    private String formatSites() {
        if (sites.isEmpty())
            return "";

        List<String> siteIds = sites.stream().map(UsgsSite::getNumber).toList();
        return "sites=" + String.join(",", siteIds);
    }

    private String formatParameter() {
        if (parameter == null)
            return "";

        return "&parameterCd=" + parameter.getCode();
    }

    private String formatTime() {
        if (temporalRequest == null)
            return "";

        DateTimeFormatter formatter = temporalRequest.dateTimeFormatter();
        String startTime = format(formatter, temporalRequest.getStartTime());
        String endTime = format(formatter, temporalRequest.getEndTime());
        Duration period = temporalRequest.getPeriod();

        if (!period.isZero()) {
            return "&period=" + period;
        } else if (startTime != null && endTime != null) {
            return "&startDT=" + startTime + "&endDT=" + endTime;
        } else if (startTime != null) {
            return "&startDT=" + startTime;
        }

        return "";
    }

    private static String format(DateTimeFormatter formatter, LocalDateTime ldt) {
        return ldt == null ? null : formatter.format(ldt);
    }
}
