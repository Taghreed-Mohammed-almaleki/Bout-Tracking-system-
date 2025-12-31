package com.boattracking;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Core class responsible for managing boats and monitoring their
 * locations.  It provides operations for registering boats, updating
 * their positions, checking for violations, filtering by status and
 * retrieving information about individual boats.
 */
public class BoatDetectionSystem {

    /**
     * Allowed geographical boundaries for monitored boats. Coordinates
     * roughly represent the region from Al Qunfudhah (south) to Rabigh
     * (north) along the Red Sea. These are simplified for demonstration.
     */
    /**
     * Southern latitude boundary of the monitoring region.  Exposed as a
     * public constant for use by auxiliary classes (e.g. map
     * renderers).
     */
    public static final double MIN_LAT = 18.0;

    /**
     * Northern latitude boundary of the monitoring region.
     */
    public static final double MAX_LAT = 23.0;

    /**
     * Western longitude boundary of the monitoring region.
     */
    public static final double MIN_LON = 39.0;

    /**
     * Eastern longitude boundary of the monitoring region.
     */
    public static final double MAX_LON = 42.0;

    /**
     * Operating hours during which boats are permitted.  Boats
     * operating outside of these hours will trigger a TIME_EXCEEDED
     * alert.  Times are in 24‑hour format.
     */
    private static final LocalTime START_OPERATING_TIME = LocalTime.of(6, 0);
    private static final LocalTime END_OPERATING_TIME = LocalTime.of(18, 0);

    /**
     * List of restricted zones represented as simple rectangular
     * bounding boxes.  A real implementation would likely use
     * polygons and more complex geofencing.
     */
    private final List<double[]> restrictedZones = new ArrayList<>();

    /**
     * Registry of all boats keyed by their system ID.
     */
    private final Map<String, Boat> boats = new HashMap<>();

    /**
     * Log of alerts that have been generated. This acts as a simple
     * database of violations and warnings for reporting.
     */
    private final List<Alert> alertLog = new ArrayList<>();

    /**
     * Counter used to generate unique system identifiers for new
     * boats.  Each invocation of {@link #assignIdToChip()} will
     * increment this counter.
     */
    private int nextId = 1;

    public BoatDetectionSystem() {
        // Example restricted zone: dummy fishing area within allowed region
        // Format: {minLat, maxLat, minLon, maxLon}
        restrictedZones.add(new double[]{20.5, 21.0, 40.5, 41.0});
    }

    /**
     * Generates a unique identifier for a chip.  In a real system
     * this would interface with the Communication Authority’s API.
     *
     * @return a newly generated boat ID
     */
    public synchronized String assignIdToChip() {
        return String.format("B%04d", nextId++);
    }

    /**
     * Registers a new boat in the system.  The caller must supply a
     * chip identifier; the system will assign its own internal boat
     * identifier.
     *
     * @param chipId the identifier of the chip attached to the boat
     * @return the newly created {@link Boat} instance
     */
    public Boat addBoat(String chipId) {
        Objects.requireNonNull(chipId, "chipId must not be null");
        String boatId = assignIdToChip();
        Boat boat = new Boat(boatId, chipId);
        boats.put(boatId, boat);
        return boat;
    }

    /**
     * Retrieves a boat by its system identifier.
     *
     * @param boatId the unique identifier of the boat
     * @return the {@link Boat} if found or {@code null} otherwise
     */
    public Boat getBoat(String boatId) {
        return boats.get(boatId);
    }

    /**
     * Searches for a boat by ID.  This method is provided as a synonym
     * for {@link #getBoat(String)} to align with the use case names.
     *
     * @param boatId the unique identifier of the boat
     * @return the matching {@link Boat} or {@code null}
     */
    public Boat searchBoatById(String boatId) {
        return getBoat(boatId);
    }

    /**
     * Returns the current position of a boat as a two element array
     * consisting of latitude and longitude.  If the boat does not
     * exist, {@code null} is returned.
     *
     * @param boatId the identifier of the boat
     * @return a double array {latitude, longitude} or {@code null}
     */
    public double[] displayBoatLocation(String boatId) {
        Boat boat = boats.get(boatId);
        if (boat == null) {
            return null;
        }
        return new double[]{boat.getLatitude(), boat.getLongitude()};
    }

    /**
     * Updates the location of the specified boat and evaluates its
     * status based on the new information.  This method will generate
     * alerts when violations occur.
     *
     * @param boatId   the boat's identifier
     * @param latitude the new latitude
     * @param longitude the new longitude
     * @param time     the timestamp of the position update
     * @return a list of alerts raised due to this update (empty if none)
     */
    public List<Alert> updateBoatLocation(String boatId, double latitude, double longitude, LocalDateTime time) {
        Boat boat = boats.get(boatId);
        if (boat == null) {
            return Collections.emptyList();
        }
        boat.updatePosition(latitude, longitude, time);

        List<Alert> newAlerts = new ArrayList<>();
        // Determine status and check for violations
        Status status = Status.GREEN;
        AlertType alertType = null;
        String alertMsg = null;
        // Check operating hours
        LocalTime updateTime = time.toLocalTime();
        if (updateTime.isBefore(START_OPERATING_TIME) || updateTime.isAfter(END_OPERATING_TIME)) {
            status = Status.RED;
            alertType = AlertType.TIME_EXCEEDED;
            alertMsg = String.format("Boat %s exceeded operating hours at %s", boatId, updateTime);
        } else {
            // Check allowed region
            boolean outside = latitude < MIN_LAT || latitude > MAX_LAT || longitude < MIN_LON || longitude > MAX_LON;
            if (outside) {
                status = Status.RED;
                alertType = AlertType.AREA_BREACH;
                alertMsg = String.format("Boat %s left permitted area at (%.2f, %.2f)", boatId, latitude, longitude);
            } else {
                // Check restricted zones
                for (double[] zone : restrictedZones) {
                    double zMinLat = zone[0];
                    double zMaxLat = zone[1];
                    double zMinLon = zone[2];
                    double zMaxLon = zone[3];
                    if (latitude >= zMinLat && latitude <= zMaxLat && longitude >= zMinLon && longitude <= zMaxLon) {
                        status = Status.RED;
                        alertType = AlertType.RESTRICTED_ZONE;
                        alertMsg = String.format("Boat %s entered restricted zone at (%.2f, %.2f)", boatId, latitude, longitude);
                        break;
                    }
                }
                if (status != Status.RED) {
                    // If near boundaries or close to end time, mark as YELLOW
                    boolean nearBoundary = (latitude - MIN_LAT) < 0.1 || (MAX_LAT - latitude) < 0.1 ||
                            (longitude - MIN_LON) < 0.1 || (MAX_LON - longitude) < 0.1;
                    boolean nearTime = updateTime.isAfter(END_OPERATING_TIME.minusMinutes(30));
                    status = (nearBoundary || nearTime) ? Status.YELLOW : Status.GREEN;
                }
            }
        }
        boat.setStatus(status);
        // If violation occurred (status RED), create alert
        if (status == Status.RED && alertType != null) {
            Alert alert = new Alert(boatId, alertType, alertMsg, time);
            alertLog.add(alert);
            newAlerts.add(alert);
        }
        return newAlerts;
    }

    /**
     * Returns a list of all boats currently registered in the system.
     *
     * @return an unmodifiable list of boats
     */
    public List<Boat> getAllBoats() {
        return Collections.unmodifiableList(new ArrayList<>(boats.values()));
    }

    /**
     * Prints a textual representation of all registered boats with their
     * current positions and status.  This provides a simple console
     * based “map” that operators can use to visualise boat locations
     * without a graphical interface.  Each boat is listed on its own
     * line in the form: {@code ID (lat, lon) – STATUS}.
     */
    public void displayAllBoats() {
        System.out.println("Current boat positions:");
        if (boats.isEmpty()) {
            System.out.println("  (no registered boats)");
            return;
        }
        for (Boat boat : boats.values()) {
            System.out.printf("  %s (%.2f, %.2f) – %s%n",
                    boat.getId(), boat.getLatitude(), boat.getLongitude(), boat.getStatus());
        }
    }

    /**
     * Filters boats by their status.
     *
     * @param status the status to filter by
     * @return a list of boats whose status matches the given status
     */
    public List<Boat> filterBoatsByStatus(Status status) {
        Objects.requireNonNull(status, "status must not be null");
        List<Boat> result = new ArrayList<>();
        for (Boat boat : boats.values()) {
            if (boat.getStatus() == status) {
                result.add(boat);
            }
        }
        return result;
    }

    /**
     * Returns the alert log containing all alerts generated by the
     * system.
     *
     * @return a list of {@link Alert} objects
     */
    public List<Alert> getAlertLog() {
        return Collections.unmodifiableList(alertLog);
    }
}