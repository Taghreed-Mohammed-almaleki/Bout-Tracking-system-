package com.boattracking;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a boat being monitored by the tracking system.
 *
 * <p>Each boat has a unique identifier, provided by the Communication
 * Authority, and maintains its current geographical location and
 * operational status. The status is updated whenever new location
 * information is received.</p>
 */
public class Boat {
    private final String id;
    private final String chipId;
    private double latitude;
    private double longitude;
    private Status status;
    private LocalDateTime lastUpdate;

    /**
     * Constructs a new boat with the given identifiers.
     *
     * @param id      the unique boat identifier used within the system
     * @param chipId  the identifier of the physical chip attached to the boat
     */
    public Boat(String id, String chipId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.chipId = Objects.requireNonNull(chipId, "chipId must not be null");
        this.status = Status.GREEN;
    }

    /**
     * Updates the boat's position and timestamp.  The status will need to be
     * updated by the tracking system after calling this method.
     *
     * @param latitude  the new latitude
     * @param longitude the new longitude
     * @param time      the time the update was recorded
     */
    public void updatePosition(double latitude, double longitude, LocalDateTime time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdate = time;
    }

    public String getId() {
        return id;
    }

    public String getChipId() {
        return chipId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String toString() {
        return "Boat{" +
                "id='" + id + '\'' +
                ", chipId='" + chipId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", status=" + status +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}