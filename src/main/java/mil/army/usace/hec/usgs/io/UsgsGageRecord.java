package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsgsGageRecord {
    private final UsgsSite usgsSite;
    private final ZonedDateTime[] times;
    private final long intervalSeconds;
    private final double[] values;
    private final double noDataValue;
    private final UsgsParameter parameter;

    private UsgsGageRecord(Builder builder) {
        usgsSite = builder.usgsSite;
        noDataValue = builder.noDataValue;
        parameter = builder.parameter;

        TimesValuesInterval normalized = normalize(builder.times, builder.values, noDataValue);

        times = normalized.times();
        values = normalized.values();
        intervalSeconds = normalized.intervalSeconds();
    }

    public static class Builder {
        private UsgsSite usgsSite;
        private ZonedDateTime[] times;
        private double[] values;
        private double noDataValue;
        private UsgsParameter parameter;

        public Builder setSite(UsgsSite usgsSite) {
            this.usgsSite = usgsSite;
            return this;
        }

        public Builder setTimes(ZonedDateTime[] times) {
            this.times = times;
            return this;
        }

        public Builder setValues(double[] values) {
            this.values = values;
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
            if (usgsSite == null)
                throw new IllegalStateException("Site number must be provided");
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
                .setSite(UsgsSite.empty())
                .setTimes(new ZonedDateTime[0])
                .setValues(new double[0])
                .setParameter(UsgsParameter.UNKNOWN)
                .build();
    }

    public UsgsSite getSite() {
        return usgsSite;
    }

    public UsgsParameter getParameter() {
        return parameter;
    }

    public ZonedDateTime[] getTimes() {
        return times;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public double[] getValues() {
        return values;
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
        List<ZonedDateTime> normalizedTimes = new ArrayList<>();
        List<Double> normalizedValues = new ArrayList<>();

        if (times.length == 0)
            return new TimesValuesInterval(new ZonedDateTime[]{}, new double[]{}, 0);

        long intervalSeconds = getMostCommonIntervalSeconds(times);
        ZonedDateTime minTime = times[0];
        ZonedDateTime maxTime = times[times.length - 1];

        Map<Long, Double> timeValueMap = new HashMap<>();
        for (int i = 0; i < times.length; i++) {
            timeValueMap.put(times[i].toEpochSecond(), values[i]);
        }

        ZonedDateTime time = minTime;
        while (intervalSeconds > 0 && !time.isAfter(maxTime)) {
            normalizedTimes.add(time);
            normalizedValues.add(timeValueMap.getOrDefault(time.toEpochSecond(), noDataValue));
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
