package com.boattracking;

/**
 * Enumerates the various kinds of alerts that can be raised by the
 * tracking system. Alerts are generated when boats exceed their
 * operational limits or enter restricted zones.
 */
public enum AlertType {
    /**
     * Indicates that a boat has operated beyond its allowed time.
     */
    TIME_EXCEEDED,

    /**
     * Indicates that a boat has exited the permitted geographical area.
     */
    AREA_BREACH,

    /**
     * Indicates that a boat has entered a restricted zone, such as a
     * protected fishing area.
     */
    RESTRICTED_ZONE
}