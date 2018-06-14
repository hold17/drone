package dk.localghost.hold17.autonomous_drone.controller;

import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;

/**
 * A flight controller is used to determine which direction the drone shall fly to get through a ring
 */
public interface FlightController {
    Direction getFlightDirection();
    Direction getLastKnownDirection();
    void resetFlightDirection();
}
