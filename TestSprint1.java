package com.boattracking;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests covering the functionality delivered in Sprint 1: registering a
 * new boat and retrieving its information.
 */
public class TestSprint1 {
    
    @Test
    public void testAddBoatAndRetrieve() {
        BoatDetectionSystem system = new BoatDetectionSystem();
        // Test adding a new boat
        Boat boat = system.addBoat("chipS1");
        assertNotNull("Boat should not be null", boat);
        assertEquals("Boat should be retrievable", boat, system.getBoat(boat.getId()));
        // Test retrieving boat information
        Boat retrieved = system.getBoat(boat.getId());
        assertEquals("Chip ID should match", "chipS1", retrieved.getChipId());
    }
}