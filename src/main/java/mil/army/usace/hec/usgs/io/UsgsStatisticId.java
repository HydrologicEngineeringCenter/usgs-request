package mil.army.usace.hec.usgs.io;

public enum UsgsStatisticId {
    MEAN("00003"),
    INSTANTANEOUS("00011");

    private final String code;

    UsgsStatisticId(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
