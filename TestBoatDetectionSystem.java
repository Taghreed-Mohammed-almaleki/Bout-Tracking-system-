package com.boattracking;

import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Simple test harness for verifying core functions of the boat detection
 * system using JUnit.
 */
public class TestBoatDetectionSystem {

    /**
     * Verifies that adding a new boat results in a unique identifier and
     * that the boat is stored within the system.
     */
    @Test
    public void testAddNewBoat() {
        BoatDetectionSystem system = new BoatDetectionSystem();
        Boat boat1 = system.addBoat("chipA");
        Boat boat2 = system.addBoat("chipB");
        assertNotNull("Boat1 should not be null", boat1);
        assertNotNull("Boat2 should not be null", boat2);
        assertNotEquals("Boat IDs should be unique", boat1.getId(), boat2.getId());
        assertNotNull("Boat1 should be retrievable", system.getBoat(boat1.getId()));
        assertNotNull("Boat2 should be retrievable", system.getBoat(boat2.getId()));
    }

    /**
     * Verifies that retrieving a boat by its ID returns the correct
     * instance and that the associated chip ID is preserved.
     */
    @Test
    public void testRetrieveBoatInformation() {
        BoatDetectionSystem system = new BoatDetectionSystem();
        Boat boat = system.addBoat("chipX");
        Boat retrieved = system.getBoat(boat.getId());
        assertNotNull("Retrieved boat should not be null", retrieved);
        assertEquals("Boat IDs should match", boat.getId(), retrieved.getId());
        assertEquals("Chip ID should be preserved", "chipX", retrieved.getChipId());
    }

    /**
     * Verifies that searching for a boat by ID returns the same boat
     * instance that was registered.
     */
    @Test
    public void testSearchBoatById() {
        BoatDetectionSystem system = new BoatDetectionSystem();
        Boat boat = system.addBoat("chipY");
        Boat found = system.searchBoatById(boat.getId());
        assertNotNull("Search should return a boat", found);
        assertEquals("Returned boat ID should match", boat.getId(), found.getId());
    }

    /**
     * Verifies that when a boat moves outside the permitted area an
     * appropriate alert is generated and the boat status becomes RED.
     */
    @Test
    public void testDetectBoatLocationAndAlert() {
        BoatDetectionSystem system = new BoatDetectionSystem();
        Boat boat = system.addBoat("chipZ");
        // Update location inside allowed area and hours
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 10, 0);
        system.updateBoatLocation(boat.getId(), 20.0, 40.0, time);
        assertEquals("Boat should be GREEN initially", Status.GREEN, boat.getStatus());
        // Move outside allowed area to trigger AREA_BREACH
        LocalDateTime time2 = LocalDateTime.of(2025, 1, 1, 11, 0);
        system.updateBoatLocation(boat.getId(), 24.0, 43.0, time2);
        assertEquals("Boat should be RED after leaving area", Status.RED, boat.getStatus());
        assertFalse("Alert log should contain at least one alert", system.getAlertLog().isEmpty());
        Alert alert = system.getAlertLog().get(system.getAlertLog().size() - 1);
        assertEquals("Alert type should be AREA_BREACH", AlertType.AREA_BREACH, alert.getType());
        assertEquals("Alert boat ID should match", boat.getId(), alert.getBoatId());
    }
}