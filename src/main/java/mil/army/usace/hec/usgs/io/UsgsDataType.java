package mil.army.usace.hec.usgs.io;

public enum UsgsDataType {
    DAILY("dv"),
    INSTANTANEOUS("iv");

    private final String id;

    UsgsDataType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
