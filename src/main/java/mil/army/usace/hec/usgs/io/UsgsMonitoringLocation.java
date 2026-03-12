package mil.army.usace.hec.usgs.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UsgsMonitoringLocation {
    private final String agencyCode;
    private final String monitoringLocationNumber;
    private final String name;
    private final double longitude;
    private final double latitude;
    private final List<UsgsTimeZoneCode> timeZones = new ArrayList<>();

    private UsgsMonitoringLocation(String agencyCode, String monitoringLocationNumber) {
        this.agencyCode = agencyCode == null ? "" : agencyCode;
        this.monitoringLocationNumber = monitoringLocationNumber == null ? "" : monitoringLocationNumber;
        this.name = null;
        this.longitude = Double.NaN;
        this.latitude = Double.NaN;
    }

    private UsgsMonitoringLocation(String monitoringLocationId) {
        int hyphenIndex = monitoringLocationId.indexOf('-');
        if (hyphenIndex < 0) {
            throw new IllegalArgumentException(
                    "Monitoring location ID must be formatted as <agencyCode>-<id>: " + monitoringLocationId);
        }
        this.agencyCode = monitoringLocationId.substring(0, hyphenIndex);
        this.monitoringLocationNumber = monitoringLocationId.substring(hyphenIndex + 1);
        this.name = null;
        this.longitude = Double.NaN;
        this.latitude = Double.NaN;
    }

    private UsgsMonitoringLocation(Builder builder) {
        agencyCode = builder.agencyCode;
        monitoringLocationNumber = builder.monitoringLocationNumber;
        name = builder.name;
        longitude = builder.longitude;
        latitude = builder.latitude;
        timeZones.addAll(builder.timeZones);
    }

    static class Builder {
        private String monitoringLocationNumber;
        private String name;
        private String agencyCode;
        private double longitude;
        private double latitude;
        private final List<UsgsTimeZoneCode> timeZones = new ArrayList<>();

        Builder setMonitoringLocationNumber(String monitoringLocationNumber) {
            this.monitoringLocationNumber = monitoringLocationNumber;
            return this;
        }

        Builder setName(String name) {
            this.name = name;
            return this;
        }

        Builder setAgencyCode(String agencyCode) {
            this.agencyCode = agencyCode;
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

        UsgsMonitoringLocation build() {
            if (agencyCode == null || agencyCode.isEmpty())
                throw new IllegalStateException("Agency code must be provided");
            if (monitoringLocationNumber == null || monitoringLocationNumber.isEmpty())
                throw new IllegalStateException("Monitoring location number must be provided");
            return new UsgsMonitoringLocation(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    public static UsgsMonitoringLocation empty() {
        return UsgsMonitoringLocation.from("", "");
    }

    public static UsgsMonitoringLocation from(String monitoringLocationId) {
        if (monitoringLocationId == null) {
            throw new IllegalArgumentException("Monitoring location ID must not be null");
        }
        return new UsgsMonitoringLocation(monitoringLocationId);
    }

    public static UsgsMonitoringLocation from(String agencyCode, String monitoringLocationNumber) {
        return new UsgsMonitoringLocation(agencyCode, monitoringLocationNumber);
    }

    public String getAgencyCode() {
        return agencyCode;
    }

    public String getMonitoringLocationNumber() {
        return monitoringLocationNumber;
    }

    public String getMonitoringLocationId() {
        if (agencyCode == null || agencyCode.isEmpty()
                || monitoringLocationNumber == null || monitoringLocationNumber.isEmpty()) {
            return "";
        }
        return agencyCode + "-" + monitoringLocationNumber;
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
        return Collections.unmodifiableList(timeZones);
    }

    @Override
    public String toString() {
        return getMonitoringLocationId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsgsMonitoringLocation that)) return false;
        return Objects.equals(agencyCode, that.agencyCode) && Objects.equals(monitoringLocationNumber, that.monitoringLocationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agencyCode, monitoringLocationNumber);
    }
}
