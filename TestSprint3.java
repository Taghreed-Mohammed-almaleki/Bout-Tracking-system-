package com.boattracking;

import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Sprint 3: detecting boat location and generating the
 * corresponding alerts.  This suite exercises the three types of
 * violations (time exceeded, area breach, restricted zone) and
 * verifies YELLOW status near boundaries or near the end of
 * operating hours.
 */
public class TestSprint3 {
    
    @Test
    public void testAlertGeneration() {
        BoatDetectionSystem system = new BoatDetectionSystem();
        Boat boat = system.addBoat("chipS3");
        // Normal position (should be GREEN) - outside restricted zone
        system.updateBoatLocation(boat.getId(), 19.5, 40.0, LocalDateTime.of(2025,1,1,10,0));
        assertEquals("Boat should be GREEN initially", Status.GREEN, boat.getStatus());
        // Near boundary (lat close to MIN_LAT) triggers YELLOW
        system.updateBoatLocation(boat.getId(), BoatDetectionSystem.MIN_LAT + 0.05, 40.0, LocalDateTime.of(2025,1,1,11,0));
        assertEquals("Boat should be YELLOW near boundary", Status.YELLOW, boat.getStatus());
        // Near end time triggers YELLOW - use safe coordinates
        system.updateBoatLocation(boat.getId(), 19.5, 40.0, LocalDateTime.of(2025,1,1,17,45));
        assertEquals("Boat should be YELLOW near end time", Status.YELLOW, boat.getStatus());
        // Time exceeded triggers RED and TIME_EXCEEDED alert
        system.updateBoatLocation(boat.getId(), 19.5, 40.0, LocalDateTime.of(2025,1,1,19,0));
        assertEquals("Boat should be RED after time exceeded", Status.RED, boat.getStatus());
        assertEquals("Alert should be TIME_EXCEEDED", AlertType.TIME_EXCEEDED, system.getAlertLog().get(system.getAlertLog().size()-1).getType());
        // Area breach triggers RED and AREA_BREACH alert
        system.updateBoatLocation(boat.getId(), 25.0, 43.0, LocalDateTime.of(2025,1,2,10,0));
        assertEquals("Boat should be RED after area breach", Status.RED, boat.getStatus());
        assertEquals("Alert should be AREA_BREACH", AlertType.AREA_BREACH, system.getAlertLog().get(system.getAlertLog().size()-1).getType());
        // Restricted zone triggers RED and RESTRICTED_ZONE alert
        // The restricted zone was defined in the constructor: lat 20.5–21.0, lon 40.5–41.0
        system.updateBoatLocation(boat.getId(), 20.6, 40.6, LocalDateTime.of(2025,1,3,9,0));
        assertEquals("Boat should be RED after entering restricted zone", Status.RED, boat.getStatus());
        assertEquals("Alert should be RESTRICTED_ZONE", AlertType.RESTRICTED_ZONE, system.getAlertLog().get(system.getAlertLog().size()-1).getType());
    }
}