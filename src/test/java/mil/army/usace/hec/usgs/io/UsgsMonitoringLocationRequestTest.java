package mil.army.usace.hec.usgs.io;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsgsMonitoringLocationRequestTest {

    @Disabled("Live API smoke test")
    @Test
    void requestTest() {

        UsgsRequest request = UsgsMonitoringLocationRequest.builder()
                .setBoundingBox(41.0845925, 40.8611339, -78.6874107, -79.0344531)
                .build();

        String response = request.retrieve();
        List<UsgsMonitoringLocation> usgsMonitoringLocations = UsgsMonitoringLocationParser.parse(response);
        assertEquals(125, usgsMonitoringLocations.size());
    }
}
