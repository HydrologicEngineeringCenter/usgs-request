package mil.army.usace.hec.usgs.io;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class UsgsMonitoringLocationRequest extends UsgsRequest {
    private static final String USGS_MONITORING_LOCATIONS_PROP = "usgs-monitoring-locations-url";
    private static final String USGS_MONITORING_LOCATIONS_URL = "https://api.waterdata.usgs.gov/ogcapi/v0/collections/monitoring-locations/items?";

    private static final String FORMAT_JSON = "f=json";
    private static final String LIMIT_10000 = "&limit=10000";

    private final double degreesNorth;
    private final double degreesSouth;
    private final double degreesEast;
    private final double degreesWest;

    private final List<String> ids;

    UsgsMonitoringLocationRequest(Builder builder) {
        degreesNorth = builder.degreesNorth;
        degreesSouth = builder.degreesSouth;
        degreesEast = builder.degreesEast;
        degreesWest = builder.degreesWest;

        ids = List.copyOf(builder.ids);
    }

    public static class Builder {
        private double degreesNorth = Double.NaN;
        private double degreesSouth = Double.NaN;
        private double degreesEast = Double.NaN;
        private double degreesWest = Double.NaN;

        private final List<String> ids = new ArrayList<>();

        public Builder setBoundingBox(double degreesNorth, double degreesSouth, double degreesEast, double degreesWest) {
            this.degreesNorth = degreesNorth;
            this.degreesSouth = degreesSouth;
            this.degreesEast = degreesEast;
            this.degreesWest = degreesWest;
            return this;
        }

        public Builder addId(String id) {
            this.ids.add(id);
            return this;
        }

        public Builder addIds(List<String> ids) {
            this.ids.addAll(ids);
            return this;
        }

        public UsgsMonitoringLocationRequest build() {
            boolean hasBbox = !Double.isNaN(degreesNorth) && !Double.isNaN(degreesSouth)
                    && !Double.isNaN(degreesEast) && !Double.isNaN(degreesWest);
            if (ids.isEmpty() && !hasBbox) {
                throw new IllegalStateException("At least one filter must be set: IDs or bounding box");
            }
            return new UsgsMonitoringLocationRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return getUsgsMonitoringLocationsUrl() +
                FORMAT_JSON +
                formatIds() +
                formatBoundingBox() +
                LIMIT_10000;
    }

    private static String getUsgsMonitoringLocationsUrl() {
        String urlString = Config.INSTANCE.getOrDefault(USGS_MONITORING_LOCATIONS_PROP, USGS_MONITORING_LOCATIONS_URL);
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

        return String.format("&bbox=%s,%s,%s,%s", minX, minY, maxX, maxY);
    }

    private String formatIds() {
        if (ids.isEmpty())
            return "";

        return "&id=" + String.join(",", ids);
    }

    private static double round(double value, int decimalPlaces) {
        return BigDecimal.valueOf(value)
                .setScale(decimalPlaces, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
