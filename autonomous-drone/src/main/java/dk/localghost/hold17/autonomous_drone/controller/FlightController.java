package dk.localghost.hold17.autonomous_drone.controller;

import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;

/**
 * A flight controller is used to determine which direction the drone shall fly to get through a ring
 */
public interface FlightController {
    /**
     * @return the current direction the drone should fly
     */
    Direction getFlightDirection();

    /**
     * This direction cannot be UNKNOWN. It is used if the current direction is unknown, the drone is still aware of
     * which direction the target was before it moved.
     *
     * @return the last known direction (not unknown)
     */
    Direction getLastKnownDirection();

    /**
     * Resets the current flight direction.
     *
     * If the implementation says the drone should fly to the right. Then after the drone have flown to the right, the
     * direction should not still be right, since the drone have moved. Therefor this method is called after each time
     * the drone moves.
     */
    void resetFlightDirection();
}
