package mil.army.usace.hec.usgs.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsgsGageRecords {
    private final Map<String, UsgsGageRecord> siteRecordMap = new HashMap<>();

    private UsgsGageRecords(List<UsgsGageRecord> records) {
        for (UsgsGageRecord r : records) {
            siteRecordMap.put(r.getSite().getNumber(), r);
        }
    }

    public static UsgsGageRecords from(List<UsgsGageRecord> usgsGageRecords) {
        return new UsgsGageRecords(usgsGageRecords);
    }

    public UsgsGageRecord get(String siteNo) {
        return siteRecordMap.getOrDefault(siteNo, UsgsGageRecord.empty());
    }

    public int count() {
        return siteRecordMap.size();
    }

    public List<String> sites() {
        return List.copyOf(siteRecordMap.keySet());
    }
}
