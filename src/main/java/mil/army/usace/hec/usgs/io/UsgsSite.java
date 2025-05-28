package mil.army.usace.hec.usgs.io;

import java.util.ArrayList;
import java.util.List;

public class UsgsSite {
    private final String id;
    private String name;
    private double longitude;
    private double latitude;
    private final List<UsgsTimeZoneCode> timeZones = new ArrayList<>();

    private UsgsSite(String id) {
        this.id = id;
    }

    private UsgsSite(Builder builder) {
        id = builder.id;
        name = builder.name;
        longitude = builder.longitude;
        latitude = builder.latitude;
        timeZones.addAll(builder.timeZones);
    }

    static class Builder {
        private String id;
        private String name;
        private double longitude;
        private double latitude;
        private final List<UsgsTimeZoneCode> timeZones = new ArrayList<>();

        Builder setId(String id) {
            this.id = id;
            return this;
        }

        Builder setName(String name) {
            this.name = name;
            return this;
        }

        Builder setLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        Builder setLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        Builder setTimeZones(List<UsgsTimeZoneCode> timeZones) {
            this.timeZones.clear();
            this.timeZones.addAll(timeZones);
            return this;
        }

        UsgsSite build() {
            return new UsgsSite(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    public static UsgsSite empty() {
        return UsgsSite.from("");
    }

    public static UsgsSite from(String id) {
        return new UsgsSite(id);
    }

    public String getNumber() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public List<UsgsTimeZoneCode> getTimeZones() {
        return timeZones;
    }
}
