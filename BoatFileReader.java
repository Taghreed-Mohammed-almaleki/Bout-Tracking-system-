package com.boattracking;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads boat information from an input file and loads it into the system.
 * Supports CSV format with boat details including ID, chip ID, position, and timestamp.
 */
public class BoatFileReader {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Represents a boat entry from the input file.
     */
    public static class BoatEntry {
        private final String boatId;
        private final String chipId;
        private final double latitude;
        private final double longitude;
        private final LocalDateTime timestamp;
        
        public BoatEntry(String boatId, String chipId, double latitude, double longitude, LocalDateTime timestamp) {
            this.boatId = boatId;
            this.chipId = chipId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }
        
        public String getBoatId() { return boatId; }
        public String getChipId() { return chipId; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("BoatEntry{id='%s', chip='%s', lat=%.2f, lon=%.2f, time=%s}",
                    boatId, chipId, latitude, longitude, timestamp);
        }
    }
    
    /**
     * Reads boat entries from a CSV file.
     * 
     * @param filename the path to the input file
     * @return list of boat entries
     * @throws IOException if file cannot be read
     */
    public static List<BoatEntry> readBoatsFromFile(String filename) throws IOException {
        List<BoatEntry> entries = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines and comments
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                try {
                    BoatEntry entry = parseLine(line);
                    entries.add(entry);
                } catch (Exception e) {
                    System.err.printf("Warning: Skipping invalid line %d: %s (Error: %s)%n", 
                            lineNumber, line, e.getMessage());
                }
            }
        }
        
        return entries;
    }
    
    /**
     * Parses a single CSV line into a BoatEntry.
     * Format: BoatID,ChipID,Latitude,Longitude,Timestamp
     */
    private static BoatEntry parseLine(String line) {
        String[] parts = line.split(",");
        
        if (parts.length != 5) {
            throw new IllegalArgumentException("Expected 5 fields, found " + parts.length);
        }
        
        String boatId = parts[0].trim();
        String chipId = parts[1].trim();
        double latitude = Double.parseDouble(parts[2].trim());
        double longitude = Double.parseDouble(parts[3].trim());
        LocalDateTime timestamp = LocalDateTime.parse(parts[4].trim(), DATE_FORMATTER);
        
        return new BoatEntry(boatId, chipId, latitude, longitude, timestamp);
    }
    
    /**
     * Loads boats from file into the detection system.
     * 
     * @param system the boat detection system
     * @param filename the input file path
     * @return number of boats successfully loaded
     */
    public static int loadBoatsIntoSystem(BoatDetectionSystem system, String filename) {
        try {
            List<BoatEntry> entries = readBoatsFromFile(filename);
            int successCount = 0;
            
            System.out.println("\n=== Loading boats from file: " + filename + " ===");
            
            for (BoatEntry entry : entries) {
                try {
                    // Add boat with chip ID
                    Boat boat = system.addBoat(entry.getChipId());
                    
                    // Update position
                    system.updateBoatLocation(
                            boat.getId(),
                            entry.getLatitude(),
                            entry.getLongitude(),
                            entry.getTimestamp()
                    );
                    
                    System.out.printf("✓ Loaded: %s (Chip: %s) at (%.2f, %.2f) - Status: %s%n",
                            boat.getId(),
                            entry.getChipId(),
                            entry.getLatitude(),
                            entry.getLongitude(),
                            boat.getStatus());
                    
                    successCount++;
                } catch (Exception e) {
                    System.err.printf("✗ Failed to load boat %s: %s%n", entry.getBoatId(), e.getMessage());
                }
            }
            
            System.out.printf("\nSuccessfully loaded %d out of %d boats from file.%n", 
                    successCount, entries.size());
            
            return successCount;
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 0;
        }
    }
}
