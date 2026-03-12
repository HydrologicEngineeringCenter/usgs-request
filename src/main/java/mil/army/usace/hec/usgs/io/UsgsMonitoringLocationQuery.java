package mil.army.usace.hec.usgs.io;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A query that fetches all USGS monitoring locations in a given bounding box
 * that have time-series data of a specified parameter and statistic type
 * between beginTime and endTime.
 */
public final class UsgsMonitoringLocationQuery
        implements Callable<List<UsgsMonitoringLocation>> {

    private static final Logger LOGGER =
            Logger.getLogger(UsgsMonitoringLocationQuery.class.getName());

    // Bounding-box coordinates in decimal degrees
    private final double north, south, east, west;

    // The USGS parameter (p-code) and statistic type to filter on
    private final UsgsParameter parameter;
    private final UsgsStatisticId statisticId;

    // Time window for which we want to see data
    private final ZonedDateTime beginTime, endTime;

    private UsgsMonitoringLocationQuery(Builder builder) {
        this.north = builder.north;
        this.south = builder.south;
        this.east = builder.east;
        this.west = builder.west;
        this.parameter = builder.parameter;
        this.statisticId = builder.statisticId;
        this.beginTime = builder.beginTime;
        this.endTime = builder.endTime;
    }

    /**
     * Builder for {@link UsgsMonitoringLocationQuery}.
     * <p>
     * You must call setBoundingBox(...) before calling build().
     * setParameter(...), setStatisticType(...), setBeginTime(...),
     * and setEndTime(...) are optional filters.
     */
    public static class Builder {
        private double north = Double.NaN, south = Double.NaN, east = Double.NaN, west = Double.NaN;
        private UsgsParameter parameter;
        private UsgsStatisticId statisticId;
        private ZonedDateTime beginTime, endTime;

        public Builder setBoundingBox(double north,
                                      double south,
                                      double east,
                                      double west) {
            this.north = north;
            this.south = south;
            this.east = east;
            this.west = west;
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

        /**
         * Validates all required inputs and constructs the query.
         *
         * @throws IllegalStateException if any required field is missing
         *                               or invalid (e.g. beginTime after endTime).
         */
        public UsgsMonitoringLocationQuery build() {
            if (Double.isNaN(north) || Double.isNaN(south) || Double.isNaN(east) || Double.isNaN(west)) {
                throw new IllegalStateException("Bounding box must be set via setBoundingBox()");
            }
            return new UsgsMonitoringLocationQuery(this);
        }
    }

    /**
     * Entry point for callers.
     * <p>
     * 1) Retrieve metadata for all sites in the box that have the requested
     * parameter & statistic in the window.
     * 2) Fetch all locations in a single request.
     * 3) Return the results as an unmodifiable list.
     */
    @Override
    public List<UsgsMonitoringLocation> call() {
        Set<String> siteIds = fetchMetadataSiteIds();
        if (siteIds.isEmpty()) {
            LOGGER.info("No matching time-series metadata found; returning empty list");
            return List.of();
        }
        return fetchLocations(new ArrayList<>(siteIds));
    }

    /**
     * Builds & executes the metadata request, parses the JSON,
     * and returns the unique site IDs.
     */
    private Set<String> fetchMetadataSiteIds() {
        UsgsTimeSeriesMetadataRequest mdReq = UsgsTimeSeriesMetadataRequest.builder()
                .setBoundingBox(north, south, east, west)
                .setParameter(parameter)
                .setStatisticType(statisticId)
                .setBeginTime(beginTime)
                .setEndTime(endTime)
                .build();

        String response = mdReq.retrieve();
        List<UsgsTimeSeriesMetadata> meta =
                UsgsTimeSeriesMetadataParser.parse(response);

        return meta.stream()
                .map(UsgsTimeSeriesMetadata::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Fetches monitoring locations for all site IDs in a single request.
     */
    private List<UsgsMonitoringLocation> fetchLocations(List<String> siteIds) {
        UsgsMonitoringLocationRequest locReq =
                UsgsMonitoringLocationRequest.builder()
                        .addIds(siteIds)
                        .build();

        String resp = locReq.retrieve();
        return UsgsMonitoringLocationParser.parse(resp);
    }

    /**
     * Convenience factory to start the builder.
     */
    public static Builder builder() {
        return new Builder();
    }
}
