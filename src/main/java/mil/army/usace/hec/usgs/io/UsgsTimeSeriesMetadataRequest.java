package mil.army.usace.hec.usgs.io;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class UsgsTimeSeriesMetadataRequest extends UsgsRequest {

    private static final String USGS_TIME_SERIES_METADATA_PROP = "usgs-time-series-metadata-url";
    private static final String USGS_TIME_SERIES_METADATA_URL = "https://api.waterdata.usgs.gov/ogcapi/v0/collections/time-series-metadata/items?";

    private static final String FORMAT_JSON = "f=json";
    private static final String LIMIT_10000 = "&limit=10000";

    private final double degreesNorth;
    private final double degreesSouth;
    private final double degreesEast;
    private final double degreesWest;

    private final UsgsParameter parameter;
    private final UsgsStatisticId statisticId;

    private final ZonedDateTime beginTime;
    private final ZonedDateTime endTime;

    UsgsTimeSeriesMetadataRequest(Builder builder) {
        super(builder.apiKey);
        degreesNorth = builder.degreesNorth;
        degreesSouth = builder.degreesSouth;
        degreesEast = builder.degreesEast;
        degreesWest = builder.degreesWest;

        parameter = builder.parameter;
        statisticId = builder.statisticId;

        beginTime = builder.beginTime;
        endTime = builder.endTime;
    }

    public static class Builder {
        private double degreesNorth = Double.NaN;
        private double degreesSouth = Double.NaN;
        private double degreesEast = Double.NaN;
        private double degreesWest = Double.NaN;

        private UsgsParameter parameter;
        private UsgsStatisticId statisticId;

        private ZonedDateTime beginTime;
        private ZonedDateTime endTime;
        private String apiKey;

        public Builder setBoundingBox(double degreesNorth, double degreesSouth, double degreesEast, double degreesWest) {
            this.degreesNorth = degreesNorth;
            this.degreesSouth = degreesSouth;
            this.degreesEast = degreesEast;
            this.degreesWest = degreesWest;
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

        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public UsgsTimeSeriesMetadataRequest build() {
            return new UsgsTimeSeriesMetadataRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return getTimeSeriesMetadataUrl() +
                FORMAT_JSON +
                formatBoundingBox() +
                formatParameterCode() +
                formatStatisticId() +
                formatBeginTime() +
                formatEndTime() +
                LIMIT_10000 +
                formatApiKey();
    }

    private static String getTimeSeriesMetadataUrl() {
        String urlString = Config.INSTANCE.getOrDefault(USGS_TIME_SERIES_METADATA_PROP, USGS_TIME_SERIES_METADATA_URL);
        return UrlValidator.validate(urlString);
    }

    private String formatBoundingBox() {
        if (Double.isNaN(degreesNorth) || Double.isNaN(degreesSouth)
                || Double.isNaN(degreesEast) || Double.isNaN(degreesWest))
            return "";

        double minX = round(degreesWest);
        double minY = round(degreesSouth);
        double maxX = round(degreesEast);
        double maxY = round(degreesNorth);

        return String.format("&bbox=%s,%s,%s,%s", minX, minY, maxX, maxY);
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

    private String formatBeginTime() {
        if (beginTime == null)
            return "";

        // record must end after query begin (exclude records entirely before the query window)
        return "&end_utc=" + beginTime.withZoneSameInstant(ZoneOffset.UTC) + "/..";
    }

    private String formatEndTime() {
        if (endTime == null)
            return "";

        // record must begin before query end (exclude records entirely after the query window)
        return "&begin_utc=../" + endTime.withZoneSameInstant(ZoneOffset.UTC);
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(7, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
