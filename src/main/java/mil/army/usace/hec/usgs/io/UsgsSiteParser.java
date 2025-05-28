package mil.army.usace.hec.usgs.io;

import java.util.*;

public class UsgsSiteParser {

    private static final String SITE_NO = "site_no";
    private static final String STATION_NM = "station_nm";
    private static final String LONGITUDE = "dec_long_va";
    private static final String LATITUDE = "dec_lat_va";
    private static final String TIME_ZONE = "tz_cd";
    private static final String OBSERVES_DST = "local_time_fg";

    private UsgsSiteParser() {
    }

    public static List<UsgsSite> parse(String response) {
        List<UsgsSite> sites = new ArrayList<>();

        List<String> lines = Arrays.stream(response.split("\\r?\\n"))
                .filter(line -> !line.startsWith("#"))
                .toList();

        if (lines.isEmpty())
            return Collections.emptyList();

        String line0 = lines.get(0);
        List<String> parts0 = List.of(line0.split("\t"));

        int[] indexStore = new int[6];
        indexStore[0] = parts0.indexOf(SITE_NO);
        indexStore[1] = parts0.indexOf(STATION_NM);
        indexStore[2] = parts0.indexOf(LONGITUDE);
        indexStore[3] = parts0.indexOf(LATITUDE);
        indexStore[4] = parts0.indexOf(TIME_ZONE);
        indexStore[5] = parts0.indexOf(OBSERVES_DST);

        for (int i = 2; i < lines.size(); i++) {
            String line = lines.get(i);
            List<String> parts = List.of(line.split("\t"));

            int indexStore0 = indexStore[0];
            int indexStore1 = indexStore[1];
            int indexStore2 = indexStore[2];
            int indexStore3 = indexStore[3];
            int indexStore4 = indexStore[4];
            int indexStore5 = indexStore[5];

            String siteNo = indexStore0 >= 0 ? parts.get(indexStore0) : "";
            String siteName = indexStore1 >= 0 ? parts.get(indexStore1) : "";
            double longitude = indexStore2 >= 0 ? parseDouble(parts.get(indexStore2)) : Double.NaN;
            double latitude = indexStore3 >= 0 ? parseDouble(parts.get(indexStore3)) : Double.NaN;
            UsgsTimeZoneCode timeZone = indexStore4 >= 0 ? UsgsTimeZoneCode.parse(parts.get(indexStore4)) : UsgsTimeZoneCode.UNDEFINED;
            boolean isObservesDST = indexStore5 >= 0 && parseObservesDST(parts.get(indexStore5));

            List<UsgsTimeZoneCode> timeZones = new ArrayList<>();
            timeZones.add(timeZone);

            if (isObservesDST) {
                Optional.ofNullable(timeZone.getDst()).ifPresent(timeZones::add);
            }

            UsgsSite usgsSite = UsgsSite.builder()
                    .setId(siteNo)
                    .setName(siteName)
                    .setLongitude(longitude)
                    .setLatitude(latitude)
                    .setTimeZones(timeZones)
                    .build();

            sites.add(usgsSite);
        }

        return sites;
    }

    private static double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private static boolean parseObservesDST(String str) {
        return str.equals("Y");
    }
}
