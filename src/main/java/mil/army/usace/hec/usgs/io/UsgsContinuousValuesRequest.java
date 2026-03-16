package mil.army.usace.hec.usgs.io;

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
}
