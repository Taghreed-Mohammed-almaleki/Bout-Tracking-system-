package com.boattracking;

import java.util.List;

/**
 * Provides a very simple textual “map” for visualising boat positions.  The
 * allowed region is rendered as a grid where each cell corresponds to a
 * portion of the monitored area.  Boats are plotted within the grid using
 * the first letter of their status: G (green), Y (yellow) or R (red).
 *
 * <p>This class is not intended to be a full replacement for a graphical
 * interface, but it allows developers to quickly assess whether boats
 * remain within boundaries when running the system from the console.</p>
 */
public class ConsoleMap {

    /**
     * Renders the given list of boats on a simple ASCII map.  The map is
     * divided into rows and columns; each boat’s latitude and longitude
     * determine which cell it occupies.
     *
     * @param boats list of boats to display
     * @param rows  number of rows in the map
     * @param cols  number of columns in the map
     */
    public static void printMap(List<Boat> boats, int rows, int cols) {
        // Determine bounds from BoatDetectionSystem constants
        double minLat = BoatDetectionSystem.MIN_LAT;
        double maxLat = BoatDetectionSystem.MAX_LAT;
        double minLon = BoatDetectionSystem.MIN_LON;
        double maxLon = BoatDetectionSystem.MAX_LON;
        char[][] grid = new char[rows][cols];
        // fill with dots
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = '.';
            }
        }
        for (Boat boat : boats) {
            // normalise coordinates
            double latRatio = (boat.getLatitude() - minLat) / (maxLat - minLat);
            double lonRatio = (boat.getLongitude() - minLon) / (maxLon - minLon);
            int r = rows - 1 - (int) Math.floor(latRatio * rows);
            int c = (int) Math.floor(lonRatio * cols);
            if (r < 0 || r >= rows || c < 0 || c >= cols) {
                continue; // skip boats outside region
            }
            // Use first letter of status
            char marker;
            switch (boat.getStatus()) {
                case GREEN:
                    marker = 'G';
                    break;
                case YELLOW:
                    marker = 'Y';
                    break;
                case RED:
                default:
                    marker = 'R';
                    break;
            }
            grid[r][c] = marker;
        }
        // Print the grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                System.out.print(grid[r][c]);
                System.out.print(' ');
            }
            System.out.println();
        }
    }
}