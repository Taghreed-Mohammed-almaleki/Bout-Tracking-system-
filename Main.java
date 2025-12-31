package com.boattracking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import javafx.application.Application;

/**
 * Demonstrates the functionality of the boat detection system across
 * three sprints.  Each stage calls the relevant methods and prints
 * results to the console.  This class acts as a simple driver and
 * makes it easy to trace progress across sprints.
 */
public class Main {
    
    private static final String DEFAULT_INPUT_FILE = "boats_input.csv";
    
    public static void main(String[] args) {
        BoatDetectionSystem system = new BoatDetectionSystem();
        
        // Ask administrator if they want to load from file
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║     Boat Tracking System - Administrator Panel           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Choose input method:");
        System.out.println("  1. Load boats from input file (" + DEFAULT_INPUT_FILE + ")");
        System.out.println("  2. Run manual test scenarios (Sprint 1-3)");
        System.out.print("\nEnter your choice (1 or 2): ");
        
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim();
        System.out.println();
        
        if (choice.equals("1")) {
            // Load from file
            int loadedCount = BoatFileReader.loadBoatsIntoSystem(system, DEFAULT_INPUT_FILE);
            
            if (loadedCount > 0) {
                System.out.println("\n" + "=".repeat(60));
                system.displayAllBoats();
                
                // Show any alerts
                List<Alert> alerts = system.getAlertLog();
                if (!alerts.isEmpty()) {
                    System.out.println("\n⚠️  Alerts detected:");
                    for (Alert alert : alerts) {
                        System.out.println("  " + alert);
                    }
                }
                
                // Launch map
                System.out.println("\n🗺️  Launching interactive map...");
                MapView.setSystem(system);
                Application.launch(MapView.class);
            } else {
                System.out.println("\n❌ No boats loaded. Please check the input file.");
                System.out.println("Expected file: " + DEFAULT_INPUT_FILE);
                System.out.println("\nFalling back to manual test mode...\n");
                runManualTests(system);
            }
        } else {
            // Run manual tests
            runManualTests(system);
        }
    }
    
    /**
     * Runs the original manual test scenarios (Sprint 1-3)
     */
    private static void runManualTests(BoatDetectionSystem system) {
        System.out.println("\n=== Sprint 1: Add a new boat & Retrieve boat information ===");
        Boat boat1 = system.addBoat("chip001");
        System.out.printf("Added boat %s with chip %s%n", boat1.getId(), boat1.getChipId());
        Boat retrieved = system.getBoat(boat1.getId());
        System.out.printf("Retrieved boat: %s%n", retrieved);
        system.displayAllBoats();

        System.out.println("\n=== Sprint 2: Search by ID & Display location ===");
        // Add another boat
        Boat boat2 = system.addBoat("chip002");
        // Update positions
        system.updateBoatLocation(boat1.getId(), 20.0, 40.0, LocalDateTime.of(2025, 1, 1, 10, 0));
        system.updateBoatLocation(boat2.getId(), 22.0, 41.0, LocalDateTime.of(2025, 1, 1, 11, 0));
        // Search for a boat
        Boat searchResult = system.searchBoatById(boat2.getId());
        System.out.printf("Search result for %s: %s%n", boat2.getId(), searchResult);
        // Display a specific boat location
        double[] loc = system.displayBoatLocation(boat1.getId());
        System.out.printf("Location of %s: (%.2f, %.2f)%n", boat1.getId(), loc[0], loc[1]);
        // Show all boats and their status
        system.displayAllBoats();

        System.out.println("\n=== Sprint 3: Detect boat location & Alerts ===");
        // Normal update (inside area)
        system.updateBoatLocation(boat1.getId(), 20.5, 40.5, LocalDateTime.of(2025, 1, 2, 10, 0));
        // Near boundary (yellow)
        system.updateBoatLocation(boat1.getId(), BoatDetectionSystem.MIN_LAT + 0.05, 40.5, LocalDateTime.of(2025, 1, 2, 11, 0));
        // Area breach (red)
        List<Alert> alerts = system.updateBoatLocation(boat1.getId(), 25.0, 43.0, LocalDateTime.of(2025, 1, 2, 12, 0));
        if (!alerts.isEmpty()) {
            System.out.println("Alerts raised:");
            for (Alert a : alerts) {
                System.out.println("  " + a);
            }
        }
        // Display final state of all boats
        system.displayAllBoats();
        // Draw simple map
        System.out.println("\nASCII Map:");
        ConsoleMap.printMap(system.getAllBoats(), 10, 20);
        
        MapView.setSystem(system);
        Application.launch(MapView.class);
    }
}