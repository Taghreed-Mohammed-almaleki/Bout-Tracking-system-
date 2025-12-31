package com.boattracking;

import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Sprint 2: searching for a boat by ID and displaying boat
 * location on the map.  These tests also exercise filtering by
 * status.
 */
public class TestSprint2 {
    
    @Test
    public void testSearchAndDisplayLocation() {
        BoatDetectionSystem system = new BoatDetectionSystem();
        // Register two boats
        Boat b1 = system.addBoat("chipA2");
        Boat b2 = system.addBoat("chipB2");
        // Assign different locations
        system.updateBoatLocation(b1.getId(), 20.0, 40.0, LocalDateTime.of(2025,1,1,10,0));
        system.updateBoatLocation(b2.getId(), 22.0, 41.0, LocalDateTime.of(2025,1,1,10,30));
        // Search by ID
        assertEquals("Search should find b1", b1, system.searchBoatById(b1.getId()));
        assertEquals("Search should find b2", b2, system.searchBoatById(b2.getId()));
        // Display location
        double[] loc1 = system.displayBoatLocation(b1.getId());
        double[] loc2 = system.displayBoatLocation(b2.getId());
        assertEquals("Latitude of b1 should match", 20.0, loc1[0], 0.001);
        assertEquals("Longitude of b1 should match", 40.0, loc1[1], 0.001);
        assertEquals("Latitude of b2 should match", 22.0, loc2[0], 0.001);
        assertEquals("Longitude of b2 should match", 41.0, loc2[1], 0.001);
        // Filter by status (all GREEN)
        assertEquals("Both boats should be green", 2, system.filterBoatsByStatus(Status.GREEN).size());
    }
}