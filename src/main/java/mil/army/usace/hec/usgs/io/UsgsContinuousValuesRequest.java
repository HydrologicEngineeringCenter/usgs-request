package mil.army.usace.hec.usgs.io;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

class UsgsContinuousValuesRequest extends UsgsValuesRequest {
    private static final String USGS_CONTINUOUS_PROP = "usgs-continuous-url";
    private static final String USGS_CONTINUOUS_URL = "https://api.waterdata.usgs.gov/ogcapi/v0/collections/continuous/items?";
    private static final Duration MAX_WINDOW = Duration.ofDays(365);

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

        Duration span = Duration.between(begin, end);
        if (span.compareTo(MAX_WINDOW) <= 0) {
            return List.of(toString());
        }

        List<String> urls = new ArrayList<>();
        ZonedDateTime chunkBegin = begin;
        while (chunkBegin.isBefore(end)) {
            ZonedDateTime chunkEnd = chunkBegin.plus(MAX_WINDOW);
            if (chunkEnd.isAfter(end)) {
                chunkEnd = end;
            }
            urls.add(buildUrlForWindow(chunkBegin, chunkEnd));
            // OGC time= is inclusive on both ends; advance past the boundary so
            // the sample at chunkEnd isn't returned twice across adjacent chunks.
            chunkBegin = chunkEnd.plusSeconds(1);
        }
        return urls;
    }
}
