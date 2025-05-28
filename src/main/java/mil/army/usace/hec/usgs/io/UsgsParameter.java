package mil.army.usace.hec.usgs.io;

public enum UsgsParameter {
    // Discharge, cfs https://help.waterdata.usgs.gov/code/parameter_cd_nm_query?parm_nm_cd=00060&fmt=html
    DISCHARGE_CFS("00060", "CFS"),
    // Gage height, ft https://help.waterdata.usgs.gov/code/parameter_cd_nm_query?parm_nm_cd=00065&fmt=html
    STAGE_FT("00065", "FT"),
    UNKNOWN("", "");

    private final String code;
    private final String units;

    UsgsParameter(String code, String units) {
        this.code = code;
        this.units = units;
    }

    public String getCode() {
        return code;
    }

    public String getUnits() {
        return units;
    }

    public static UsgsParameter fromCode(String code) {
        for (UsgsParameter value : values()) {
            if (value.code.equals(code))
                return value;
        }

        return UNKNOWN;
    }
}
