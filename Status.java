package com.boattracking;

/**
 * Enumeration representing the operational status of a boat.
 *
 * <p>The status is determined based on whether a boat is within the
 * allowed geographical region and operating hours.  A GREEN status
 * indicates the boat is operating normally, YELLOW means the boat is
 * approaching its limits, and RED signifies a violation has occurred.</p>
 */
public enum Status {
    /**
     * Boat is operating within its permitted area and time.
     */
    GREEN,

    /**
     * Boat is close to breaching either the time or area constraints.
     */
    YELLOW,

    /**
     * Boat has exceeded its allowed time or left the permitted area.
     */
    RED
}