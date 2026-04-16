package mil.army.usace.hec.usgs.io;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

class UsgsContinuousValuesRequest extends UsgsValuesRequest {
    private static final String USGS_CONTINUOUS_PROP = "usgs-continuous-url";
    private static final String USGS_CONTINUOUS_URL = "https://api.waterdata.usgs.gov/ogcapi/v0/collections/continuous/items?";

    UsgsContinuousValuesRequest(Builder builder) {
        super(builder);
    }

    String getServiceUrl() {
        String urlString = Config.INSTANCE.getOrDefault(USGS_CONTINUOUS_PROP, USGS_CONTINUOUS_URL);
        return UrlValidator.validate(urlString);
    }

    @Override
    protected List<String> buildRequestUrls() {
        ZonedDateTime begin = getBeginTime();
        ZonedDateTime end = getEndTime();
        if (begin == null || end == null) {
            return List.of(toString());
        }

        List<ZonedDateTime[]> chunks = splitByWaterYear(begin, end);
        if (chunks.size() == 1) {
            return List.of(toString());
        }

        List<String> urls = new ArrayList<>();
        ZonedDateTime priorEnd = null;
        for (ZonedDateTime[] chunk : chunks) {
            // OGC time= is inclusive on both ends; advance past the prior boundary so
            // the sample at chunkEnd isn't returned twice across adjacent chunks.
            ZonedDateTime chunkBegin = (priorEnd == null) ? chunk[0] : priorEnd.plusSeconds(1);
            ZonedDateTime chunkEnd = chunk[1];
            urls.add(buildUrlForWindow(chunkBegin, chunkEnd));
            priorEnd = chunkEnd;
        }
        return urls;
    }
}
