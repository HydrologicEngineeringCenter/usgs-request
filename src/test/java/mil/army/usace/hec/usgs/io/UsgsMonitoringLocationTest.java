package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsgsMonitoringLocationTest {

    @Test
    void fromCompositeId() {
        UsgsMonitoringLocation location = UsgsMonitoringLocation.from("USGS-03034000");

        assertEquals("USGS", location.getAgencyCode());
        assertEquals("03034000", location.getMonitoringLocationNumber());
        assertEquals("USGS-03034000", location.getMonitoringLocationId());
    }

    @Test
    void fromAgencyAndNumber() {
        UsgsMonitoringLocation location = UsgsMonitoringLocation.from("USGS", "03034000");

        assertEquals("USGS", location.getAgencyCode());
        assertEquals("03034000", location.getMonitoringLocationNumber());
        assertEquals("USGS-03034000", location.getMonitoringLocationId());
    }

    @Test
    void fromCompositeIdThrowsWithoutHyphen() {
        assertThrows(IllegalArgumentException.class, () ->
                UsgsMonitoringLocation.from("USGS03034000"));
    }

    @Test
    void emptyLocationHasEmptyId() {
        UsgsMonitoringLocation empty = UsgsMonitoringLocation.empty();
        assertEquals("", empty.getMonitoringLocationId());
    }

    @Test
    void equalsAndHashCodeByAgencyAndNumber() {
        UsgsMonitoringLocation a = UsgsMonitoringLocation.from("USGS-03034000");
        UsgsMonitoringLocation b = UsgsMonitoringLocation.from("USGS", "03034000");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void notEqualDifferentNumber() {
        UsgsMonitoringLocation a = UsgsMonitoringLocation.from("USGS-03034000");
        UsgsMonitoringLocation b = UsgsMonitoringLocation.from("USGS-01541000");

        assertNotEquals(a, b);
    }

    @Test
    void notEqualDifferentAgency() {
        UsgsMonitoringLocation a = UsgsMonitoringLocation.from("USGS", "03034000");
        UsgsMonitoringLocation b = UsgsMonitoringLocation.from("OTHER", "03034000");

        assertNotEquals(a, b);
    }

    @Test
    void toStringReturnsCompositeId() {
        UsgsMonitoringLocation location = UsgsMonitoringLocation.from("USGS-03034000");
        assertEquals("USGS-03034000", location.toString());
    }

    @Test
    void getMonitoringLocationIdEmptyWhenAgencyNull() {
        UsgsMonitoringLocation location = UsgsMonitoringLocation.from("", "");
        assertEquals("", location.getMonitoringLocationId());
    }
}
