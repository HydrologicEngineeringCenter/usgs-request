package mil.army.usace.hec.usgs.io;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

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

    /**
     * Narrows a caller-requested [queryBegin, queryEnd] window to the envelope of
     * actual period-of-record across the supplied metadata. The returned window is
     * the intersection of the query window with [min(beginTimes), max(endTimes)].
     * <p>
     * Use this to avoid issuing values requests for time ranges where no site has
     * data. Returns an empty Optional when the intersection is empty (no overlap)
     * or the metadata collection has no begin/end times to bound the query.
     *
     * @param metadata   period-of-record entries returned from the metadata query
     * @param queryBegin caller's requested begin, or null for open-ended
     * @param queryEnd   caller's requested end, or null for open-ended
     * @return narrowed [begin, end] window, or empty if there is no overlap
     */
    public static Optional<ZonedDateTime[]> narrowWindow(Collection<UsgsTimeSeriesMetadata> metadata,
                                                         ZonedDateTime queryBegin,
                                                         ZonedDateTime queryEnd) {
        if (metadata == null || metadata.isEmpty()) {
            return Optional.empty();
        }

        ZonedDateTime envelopeBegin = metadata.stream()
                .map(UsgsTimeSeriesMetadata::getBeginTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        ZonedDateTime envelopeEnd = metadata.stream()
                .map(UsgsTimeSeriesMetadata::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        ZonedDateTime narrowedBegin = latest(queryBegin, envelopeBegin);
        ZonedDateTime narrowedEnd = earliest(queryEnd, envelopeEnd);

        if (narrowedBegin == null || narrowedEnd == null || !narrowedBegin.isBefore(narrowedEnd)) {
            return Optional.empty();
        }
        return Optional.of(new ZonedDateTime[]{narrowedBegin, narrowedEnd});
    }

    private static ZonedDateTime latest(ZonedDateTime a, ZonedDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private static ZonedDateTime earliest(ZonedDateTime a, ZonedDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }
}
