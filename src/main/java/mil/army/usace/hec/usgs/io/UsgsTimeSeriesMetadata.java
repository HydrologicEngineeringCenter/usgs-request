package mil.army.usace.hec.usgs.io;

import java.time.ZonedDateTime;
import java.util.Objects;

public class UsgsTimeSeriesMetadata {
    private final String id;
    private final String statisticId;
    private final ZonedDateTime beginTime;
    private final ZonedDateTime endTime;

    private UsgsTimeSeriesMetadata(Builder builder) {
        this.id = builder.id;
        this.statisticId = builder.statisticId;
        this.beginTime = builder.beginTime;
        this.endTime = builder.endTime;
    }

    /* ------------------------------------------------------------------
     *  Builder
     * ------------------------------------------------------------------ */
    public static class Builder {
        private String id;
        private String statisticId;
        private ZonedDateTime beginTime;
        private ZonedDateTime endTime;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setStatisticId(String statisticId) {
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

        public UsgsTimeSeriesMetadata build() {
            Objects.requireNonNull(id, "id must not be null");

            if (beginTime != null && endTime != null && beginTime.isAfter(endTime)) {
                throw new IllegalArgumentException(
                        "beginTime must not be after endTime");
            }
            return new UsgsTimeSeriesMetadata(this);
        }
    }

    /* ------------------------------------------------------------------
     *  Convenient entry point
     * ------------------------------------------------------------------ */
    public static Builder builder() {
        return new Builder();
    }

    /* ------------------------------------------------------------------
     *  Accessors
     * ------------------------------------------------------------------ */
    public String getId() {
        return id;
    }

    public String getStatisticId() {
        return statisticId;
    }

    public ZonedDateTime getBeginTime() {
        return beginTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }
}
