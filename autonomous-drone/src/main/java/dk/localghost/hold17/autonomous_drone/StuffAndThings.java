package dk.localghost.hold17.autonomous_drone;

import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;

public class StuffAndThings {

    public static void main(String[] args) {
        final IARDrone drone = new ARDrone("10.0.1.2");

        drone.start();

        CircleChase cc = new CircleChase(drone);

        cc.animateLeds();
    }
}
