package dk.localghost.hold17.autonomous_drone;

import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.LEDAnimation;

public class CircleChase {
    private IARDrone drone = null;

    public CircleChase(IARDrone drone) {
        this.drone = drone;
    }

    public void animateLeds() {
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN_RED, 3, 10);
        drone.hover();
    }
}
