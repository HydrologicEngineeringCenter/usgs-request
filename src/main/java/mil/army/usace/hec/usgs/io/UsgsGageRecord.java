package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

public class UsgsGageRecord {
    private final UsgsMonitoringLocation usgsMonitoringLocation;
    private final ZonedDateTime[] times;
    private final long intervalSeconds;
    private final double[] values;
    private final double noDataValue;
    private final UsgsParameter parameter;

    private UsgsGageRecord(Builder builder) {
        usgsMonitoringLocation = builder.usgsMonitoringLocation;
        noDataValue = builder.noDataValue;
        parameter = builder.parameter;

        TimesValuesInterval normalized = normalize(builder.times, builder.values, noDataValue);

        times = normalized.times();
        values = normalized.values();
        intervalSeconds = normalized.intervalSeconds();
    }

    public static class Builder {
        private UsgsMonitoringLocation usgsMonitoringLocation;
        private ZonedDateTime[] times;
        private double[] values;
        private double noDataValue;
        private UsgsParameter parameter;

        public Builder setMonitoringLocation(UsgsMonitoringLocation usgsMonitoringLocation) {
            this.usgsMonitoringLocation = usgsMonitoringLocation;
            return this;
        }

        public Builder setTimes(ZonedDateTime[] times) {
            this.times = times.clone();
            return this;
        }

        public Builder setValues(double[] values) {
            this.values = values.clone();
            return this;
        }

        public Builder setNoDataValue(double noDataValue) {
            this.noDataValue = noDataValue;
            return this;
        }

        public Builder setParameter(UsgsParameter parameter) {
            this.parameter = parameter;
            return this;
        }

        public UsgsGageRecord build() {
            if (usgsMonitoringLocation == null)
                throw new IllegalStateException("Monitoring location must be provided");
            if (parameter == null)
                throw new IllegalStateException("Parameter must be provided");
            if (times == null)
                throw new IllegalStateException("Times array must be provided");
            if (values == null)
                throw new IllegalStateException("Values array must be provided");
            if (times.length != values.length)
                throw new IllegalStateException("Times and values arrays must have equal length");

            return new UsgsGageRecord(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static UsgsGageRecord empty() {
        return UsgsGageRecord.builder()
                .setMonitoringLocation(UsgsMonitoringLocation.empty())
                .setTimes(new ZonedDateTime[0])
                .setValues(new double[0])
                .setParameter(UsgsParameter.UNKNOWN)
                .build();
    }

    public UsgsMonitoringLocation getMonitoringLocation() {
        return usgsMonitoringLocation;
    }

    public UsgsParameter getParameter() {
        return parameter;
    }

    public ZonedDateTime[] getTimes() {
        return times.clone();
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public double[] getValues() {
        return values.clone();
    }

    public double getNoDataValue() {
        return noDataValue;
    }

    public int size() {
        return values.length;
    }

    private static long getMostCommonIntervalSeconds(ZonedDateTime[] times) {
        if (times.length == 0)
            return -1;

        Map<Long, Integer> frequencyMap = new HashMap<>();

        for (int i = 1; i < times.length; i++) {
            ZonedDateTime timeIM1 = times[i - 1];
            ZonedDateTime timeI = times[i];
            long interval = Duration.between(timeIM1, timeI).toSeconds();
            frequencyMap.put(interval, frequencyMap.getOrDefault(interval, 0) + 1);
        }

        return frequencyMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1L);
    }

    private static TimesValuesInterval normalize(ZonedDateTime[] times, double[] values, double noDataValue) {
        if (times.length == 0)
            return new TimesValuesInterval(new ZonedDateTime[]{}, new double[]{}, 0);

        if (times.length == 1)
            return new TimesValuesInterval(
                    new ZonedDateTime[]{times[0]},
                    new double[]{values[0]},
                    0);

        // Sort by time, keeping time-value pairs together
        Integer[] indices = new Integer[times.length];
        for (int i = 0; i < indices.length; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparing(a -> times[a]));

        ZonedDateTime[] sortedTimes = new ZonedDateTime[times.length];
        double[] sortedValues = new double[times.length];
        for (int i = 0; i < indices.length; i++) {
            sortedTimes[i] = times[indices[i]];
            sortedValues[i] = values[indices[i]];
        }

        long intervalSeconds = getMostCommonIntervalSeconds(sortedTimes);
        ZonedDateTime minTime = sortedTimes[0];
        ZonedDateTime maxTime = sortedTimes[sortedTimes.length - 1];

        // Use Instant keys for zone-safe lookups (ZonedDateTime.equals considers zone identity)
        Map<Instant, Double> instantValueMap = new HashMap<>();
        for (int i = 0; i < sortedTimes.length; i++) {
            instantValueMap.putIfAbsent(sortedTimes[i].toInstant(), sortedValues[i]);
        }

        // Guard against pathological data producing huge arrays
        long expectedSteps = intervalSeconds > 0
                ? Duration.between(minTime, maxTime).toSeconds() / intervalSeconds
                : 0;

        if (intervalSeconds <= 0 || expectedSteps > 1_000_000) {
            // Skip gap-filling but still use deduped data from the map
            ZonedDateTime[] dedupedTimes = new ZonedDateTime[instantValueMap.size()];
            double[] dedupedValues = new double[instantValueMap.size()];
            int idx = 0;
            for (ZonedDateTime sortedTime : sortedTimes) {
                Instant key = sortedTime.toInstant();
                if (instantValueMap.containsKey(key)) {
                    dedupedTimes[idx] = sortedTime;
                    dedupedValues[idx] = instantValueMap.remove(key);
                    idx++;
                }
            }
            return new TimesValuesInterval(dedupedTimes, dedupedValues, intervalSeconds);
        }

        List<ZonedDateTime> normalizedTimes = new ArrayList<>();
        List<Double> normalizedValues = new ArrayList<>();

        ZonedDateTime time = minTime;
        while (!time.isAfter(maxTime)) {
            normalizedTimes.add(time);
            normalizedValues.add(instantValueMap.getOrDefault(time.toInstant(), noDataValue));
            time = time.plusSeconds(intervalSeconds);
        }

        return new TimesValuesInterval(
                normalizedTimes.toArray(ZonedDateTime[]::new),
                normalizedValues.stream().mapToDouble(Double::doubleValue).toArray(),
                intervalSeconds
        );
    }

    private record TimesValuesInterval(ZonedDateTime[] times, double[] values, long intervalSeconds) {
    }
}
