package mil.army.usace.hec.usgs.io;

import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Time zone codes based on USGS data:
 * <a href="https://help.waterdata.usgs.gov/code/tz_query?fmt=html">...</a>
 */
public enum UsgsTimeZoneCode {
    NZDT("NZDT", "New Zealand Daylight Time", ZoneOffset.ofHours(13)),
    IDLE("IDLE", "International Date Line, East", ZoneOffset.ofHours(12)),
    NZST("NZST", "New Zealand Standard Time", ZoneOffset.ofHours(12)),
    NZT("NZT", "New Zealand Time", ZoneOffset.ofHours(12)),
    AESST("AESST", "Australia Eastern Standard Time", ZoneOffset.ofHours(11)),
    EASST("EASST", "East Australian Summer Time", ZoneOffset.ofHours(11)),
    ZP11("ZP11", "UTC +11 hours", ZoneOffset.ofHours(11)),
    ACSST("ACSST", "Central Australia Summer Time", ZoneOffset.ofHoursMinutes(10, 30)),
    CADT("CADT", "Central Australia Daylight Time", ZoneOffset.ofHoursMinutes(10, 30)),
    SADT("SADT", "South Australian Daylight Time", ZoneOffset.ofHoursMinutes(10, 30)),
    AEST("AEST", "Australia Eastern Standard Time", ZoneOffset.ofHours(10)),
    EAST("EAST", "East Australian Standard Time", ZoneOffset.ofHours(10)),
    GST("GST", "Guam Standard Time", ZoneOffset.ofHours(10)),
    LIGT("LIGT", "Melbourne, Australia", ZoneOffset.ofHours(10)),
    ACST("ACST", "Central Australia Standard Time", ZoneOffset.ofHoursMinutes(9, 30)),
    CAST("CAST", "Central Australia Standard Time", ZoneOffset.ofHoursMinutes(9, 30)),
    SAT("SAT", "South Australian Standard Time", ZoneOffset.ofHoursMinutes(9, 30)),
    AWSST("AWSST", "Australia Western Summer Time", ZoneOffset.ofHours(9)),
    JST("JST", "Japan Standard Time", ZoneOffset.ofHours(9)),
    KST("KST", "Korea Standard Time", ZoneOffset.ofHours(9)),
    WDT("WDT", "West Australian Daylight Time", ZoneOffset.ofHours(9)),
    MT("MT", "Moluccas Time", ZoneOffset.ofHoursMinutes(8, 30)),
    AWST("AWST", "Australia Western Standard Time", ZoneOffset.ofHours(8)),
    CCT("CCT", "China Coastal Time", ZoneOffset.ofHours(8)),
    WST("WST", "West Australian Standard Time", ZoneOffset.ofHours(8)),
    WADT("WADT", "West Australian Daylight Time", ZoneOffset.ofHours(8)),
    JT("JT", "Java Time", ZoneOffset.ofHoursMinutes(7, 30)),
    WAST("WAST", "West Australian Standard Time", ZoneOffset.ofHours(7)),
    ZP6("ZP6", "UTC +6 hours", ZoneOffset.ofHours(6)),
    ZP5("ZP5", "UTC +5 hours", ZoneOffset.ofHours(5)),
    AFT("AFT", "Afghanistan Time", ZoneOffset.ofHours(4)),
    ZP4("ZP4", "UTC +4 hours", ZoneOffset.ofHours(4)),
    BT("BT", "Baghdad Time", ZoneOffset.ofHours(3)),
    EETDST("EETDST", "Eastern Europe Daylight Time", ZoneOffset.ofHours(3)),
    IT("IT", "Iran Time", ZoneOffset.ofHours(3)),
    CETDST("CETDST", "Central European Daylight Time", ZoneOffset.ofHours(2)),
    SST("SST", "Swedish Summer Time", ZoneOffset.ofHours(2)),
    IST("IST", "Israel Standard Time", ZoneOffset.ofHours(2)),
    FWT("FWT", "French Winter Time", ZoneOffset.ofHours(2)),
    EET("EET", "Eastern Europe Standard Time", ZoneOffset.ofHours(2)),
    MEST("MEST", "Middle Europe Summer Time", ZoneOffset.ofHours(2)),
    METDST("METDST", "Middle Europe Daylight Time", ZoneOffset.ofHours(2)),
    BST("BST", "British Summer Time", ZoneOffset.ofHours(1)),
    CET("CET", "Central European Time", ZoneOffset.ofHours(1)),
    DNT("DNT", "Dansk Normal Time", ZoneOffset.ofHours(1)),
    DST("DST", "Dansk Summer Time", ZoneOffset.ofHours(1)),
    FST("FST", "French Summer Time", ZoneOffset.ofHours(1)),
    MET("MET", "Middle Europe Time", ZoneOffset.ofHours(1)),
    MEWT("MEWT", "Middle Europe Winter Time", ZoneOffset.ofHours(1)),
    MEZ("MEZ", "Middle Europe Zone", ZoneOffset.ofHours(1)),
    NOR("NOR", "Norway Standard Time", ZoneOffset.ofHours(1)),
    SET("SET", "Seychelles Time", ZoneOffset.ofHours(1)),
    SWT("SWT", "Swedish Winter Time", ZoneOffset.ofHours(1)),
    WETDST("WETDST", "Western Europe Daylight Time", ZoneOffset.ofHours(1)),
    GMT("GMT", "Greenwich Mean Time", ZoneOffset.ofHours(0)),
    UTC("UTC", "Universal Coordinated Time", ZoneOffset.UTC),
    WET("WET", "Western Europe", ZoneOffset.ofHours(0)),
    WAT("WAT", "West Africa Time", ZoneOffset.ofHours(-1)),
    ZP_MINUS_2("ZP-2", "UTC -2 hours", ZoneOffset.ofHours(-2)),
    NDT("NDT", "Newfoundland Daylight Time", ZoneOffset.ofHoursMinutes(-2, -30)),
    ZP_MINUS_3("ZP-3", "UTC -3 hours", ZoneOffset.ofHours(-3)),
    ADT("ADT", "Atlantic Daylight Time", ZoneOffset.ofHours(-3)),
    NFT("NFT", "Newfoundland Standard Time", ZoneOffset.ofHoursMinutes(-3, -30)),
    NST("NST", "Newfoundland Standard Time", ZoneOffset.ofHoursMinutes(-3, -30)),
    EDT("EDT", "Eastern Daylight Time", ZoneOffset.ofHours(-4)),
    AST("AST", "Atlantic Standard Time (Canada)", ZoneOffset.ofHours(-4)),
    EST("EST", "Eastern Standard Time", ZoneOffset.ofHours(-5)),
    CDT("CDT", "Central Daylight Time", ZoneOffset.ofHours(-5)),
    CST("CST", "Central Standard Time", ZoneOffset.ofHours(-6)),
    MDT("MDT", "Mountain Daylight Time", ZoneOffset.ofHours(-6)),
    PDT("PDT", "Pacific Daylight Time", ZoneOffset.ofHours(-7)),
    MST("MST", "Mountain Standard Time", ZoneOffset.ofHours(-7)),
    PST("PST", "Pacific Standard Time", ZoneOffset.ofHours(-8)),
    AKDT("AKDT", "Alaska Daylight Time", ZoneOffset.ofHours(-8)),
    AKST("AKST", "Alaska Standard Time", ZoneOffset.ofHours(-9)),
    HDT("HDT", "Hawaii Daylight Time", ZoneOffset.ofHours(-9)),
    HST("HST", "Hawaii Standard Time", ZoneOffset.ofHours(-10)),
    ZP_MINUS_11("ZP-11", "UTC +11 hours", ZoneOffset.ofHours(-11)),
    IDLW("IDLW", "International Date Line, West", ZoneOffset.ofHours(-12)),
    UNDEFINED("UNDEFINED", "UNDEFINED", ZoneOffset.ofHours(0));

    private static final Logger LOGGER = Logger.getLogger(UsgsTimeZoneCode.class.getName());

    private final String code;
    private final String name;
    private final ZoneOffset zoneOffset;

    UsgsTimeZoneCode(String code, String name, ZoneOffset zoneOffset) {
        this.code = code;
        this.name = name;
        this.zoneOffset = zoneOffset;
    }

    @Override
    public String toString() {
        String s = "(" + zoneOffset + ") " + name;
        return s.replace("Z", "00:00");
    }

    /**
     * Retrieves the ZoneOffset associated with this time zone.
     *
     * @return The ZoneOffset object.
     */
    public ZoneOffset getZoneOffset() {
        return zoneOffset;
    }

    public boolean isDst() {
        return switch (this) {
            case ACSST, AESST, AKDT, ADT, AWSST, CADT, CETDST, CDT, EASST, EETDST, EDT, FWT, BST, HDT, METDST, MEST,
                 MDT, NDT, NZDT, PDT, SADT, SST, WADT, WETDST, WDT -> true;
            default -> false;
        };
    }

    public UsgsTimeZoneCode getDst() {
        return switch (this) {
            case ACST -> ACSST;
            case AEST -> AESST;
            case AKST -> AKDT;
            case AST -> ADT;
            case AWST -> AWSST;
            case CAST -> CADT;
            case CET -> CETDST;
            case CST -> CDT;
            case EAST -> EASST;
            case EET -> EETDST;
            case EST -> EDT;
            case FST -> FWT;
            case GMT -> BST;
            case HST -> HDT;
            case MET -> METDST;
            case MEWT -> MEST;
            case MST -> MDT;
            case NFT -> NDT;
            case NZST, NZT -> NZDT;
            case PST -> PDT;
            case SAT -> SADT;
            case SWT -> SST;
            case WAST -> WADT;
            case WET -> WETDST;
            case WST -> WDT;
            default -> null;
        };
    }

    /**
     * Parses a time zone code or name and returns the corresponding UsgsTimeZoneCode.
     *
     * @param str The time zone code or name to parse.
     * @return The matching UsgsTimeZoneCode.
     */
    public static UsgsTimeZoneCode parse(String str) {
        for (UsgsTimeZoneCode value : UsgsTimeZoneCode.values()) {
            if (value.code.equalsIgnoreCase(str) || str.toLowerCase().contains(value.name.toLowerCase()))
                return value;
        }

        LOGGER.log(Level.SEVERE, () -> "String \"" + str + "\" could not be parsed. TZ offset is 0.");
        return UsgsTimeZoneCode.UNDEFINED;
    }
}
