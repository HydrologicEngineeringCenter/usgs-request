package mil.army.usace.hec.usgs.io;

import java.util.List;

public class UsgsGageRecords {

    private final List<UsgsGageRecord> records;

    private UsgsGageRecords(List<UsgsGageRecord> records) {
        this.records = List.copyOf(records);
    }

    public static UsgsGageRecords from(List<UsgsGageRecord> usgsGageRecords) {
        return new UsgsGageRecords(usgsGageRecords);
    }

    public UsgsGageRecords filter(UsgsMonitoringLocation monitoringLocation) {
        List<UsgsGageRecord> filtered = records.stream()
                .filter(r -> r.getMonitoringLocation().equals(monitoringLocation))
                .toList();
        return new UsgsGageRecords(filtered);
    }

    public UsgsGageRecords filter(UsgsParameter parameter) {
        List<UsgsGageRecord> filtered = records.stream()
                .filter(r -> r.getParameter().equals(parameter))
                .toList();
        return new UsgsGageRecords(filtered);
    }

    public List<UsgsGageRecord> records() {
        return records;
    }

    public UsgsGageRecord first() {
        if (records.isEmpty())
            return UsgsGageRecord.empty();

        return records.get(0);
    }

    public int count() {
        return records.size();
    }

    public List<String> monitoringLocationIds() {
        return records.stream()
                .map(r -> r.getMonitoringLocation().getMonitoringLocationId())
                .distinct()
                .toList();
    }
}
