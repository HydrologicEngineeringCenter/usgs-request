package mil.army.usace.hec.usgs.io;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UsgsSiteRequest extends UsgsRequest {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String USGS_SITE_SERVICE_PROP = "usgs-site-service-url";
    private static final String USGS_SITE_SERVICE_URL = "https://waterservices.usgs.gov/nwis/site/?";

    private static final String FORMAT_RDB_1_0 = "&format=rdb,1.0";
    private static final String EXPAND_SITE_OUTPUT = "&siteOutput=expanded";

    private final double degreesNorth;
    private final double degreesSouth;
    private final double degreesEast;
    private final double degreesWest;

    private final Set<UsgsParameter> parameters;
    private final UsgsTemporalRequest temporalRequest;

    UsgsSiteRequest(Builder builder) {
        degreesNorth = builder.degreesNorth;
        degreesSouth = builder.degreesSouth;
        degreesEast = builder.degreesEast;
        degreesWest = builder.degreesWest;

        parameters = builder.parameters;
        temporalRequest = builder.temporalRequest;
    }

    public static class Builder {
        private double degreesNorth = Double.NaN;
        private double degreesSouth = Double.NaN;
        private double degreesEast = Double.NaN;
        private double degreesWest = Double.NaN;

        private final Set<UsgsParameter> parameters = new HashSet<>();
        private UsgsTemporalRequest temporalRequest;

        public Builder setBoundingBox(double degreesNorth, double degreesSouth, double degreesEast, double degreesWest) {
            this.degreesNorth = degreesNorth;
            this.degreesSouth = degreesSouth;
            this.degreesEast = degreesEast;
            this.degreesWest = degreesWest;
            return this;
        }

        public Builder addParameter(UsgsParameter parameter) {
            parameters.add(parameter);
            return this;
        }

        public Builder setTemporalRequest(UsgsTemporalRequest temporalRequest) {
            this.temporalRequest = temporalRequest;
            return this;
        }

        public UsgsSiteRequest build() {
            return new UsgsSiteRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return getUsgsSiteServiceUrl() +
                formatBoundingBox() +
                formatParameterCodes() +
                formatDate() +
                EXPAND_SITE_OUTPUT +
                FORMAT_RDB_1_0;
    }

    private static String getUsgsSiteServiceUrl() {
        String urlString = Config.INSTANCE.getOrDefault(USGS_SITE_SERVICE_PROP, USGS_SITE_SERVICE_URL);
        return UrlValidator.validate(urlString);
    }

    private String formatBoundingBox() {
        if (Double.isNaN(degreesNorth) || Double.isNaN(degreesSouth)
                || Double.isNaN(degreesEast) || Double.isNaN(degreesWest))
            return "";

        double minX = round(degreesWest, 7);
        double minY = round(degreesSouth, 7);
        double maxX = round(degreesEast, 7);
        double maxY = round(degreesNorth, 7);

        return String.format("bBox=%s,%s,%s,%s", minX, minY, maxX, maxY);
    }

    private String formatParameterCodes() {
        if (parameters.isEmpty())
            return "";

        return "&parameterCd=" + parameters.stream()
                .map(UsgsParameter::getCode)
                .collect(Collectors.joining(","));
    }

    private String formatDate() {
        if (temporalRequest == null)
            return "";

        String startTime = format(temporalRequest.getStartTime());
        String endTime = format(temporalRequest.getEndTime());
        Duration period = temporalRequest.getPeriod();

        String dataTypeId = temporalRequest.getDataType().getId();
        String dataType = "&hasDataTypeCd=" + dataTypeId;

        if (!period.isZero())
            return dataType + "&period=" + period;

        if (startTime != null && endTime != null)
            return dataType + "&startDT=" + startTime + "&endDT=" + endTime;

        if (startTime != null)
            return dataType + "&startDT=" + startTime;

        return "";
    }

    private static String format(LocalDateTime ldt) {
        if (ldt == null)
            return null;

        try {
            return FORMATTER.format(ldt);
        } catch (DateTimeException e) {
            return null;
        }
    }

    public static double round(double value, int decimalPlaces) {
        return BigDecimal.valueOf(value)
                .setScale(decimalPlaces, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
