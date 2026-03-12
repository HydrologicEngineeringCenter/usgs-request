package mil.army.usace.hec.usgs.io;

class UsgsDailyValuesRequest extends UsgsValuesRequest {
    private static final String USGS_DAILY_PROP = "usgs-daily-url";
    private static final String USGS_DAILY_URL = "https://api.waterdata.usgs.gov/ogcapi/v0/collections/daily/items?";

    UsgsDailyValuesRequest(Builder builder) {
        super(builder);
    }

    String getServiceUrl() {
        String urlString = Config.INSTANCE.getOrDefault(USGS_DAILY_PROP, USGS_DAILY_URL);
        return UrlValidator.validate(urlString);
    }
}
