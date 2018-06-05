package dk.localghost.hold17.autonomous_drone.controller;

import dk.localghost.hold17.base.IARDrone;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;


public class KeyboardCommandManager implements EventHandler<KeyEvent> {
    private DroneController controller;
    private IARDrone drone;

    public KeyboardCommandManager(DroneController controller) {
        this.controller = controller;
        this.drone = controller.getDrone();
    }

    @Override
    public void handle(KeyEvent event) {
        switch(event.getCode()) {
            case TAB:
                controller.reset();
                controller.LEDSuccess();
                break;
            case B:
                controller.LEDSuccess();
                break;
            case W:
                drone.forward(); break;
            case S:
                drone.backward(); break;
            case A:
                drone.goLeft(); break;
            case D:
                drone.goRight(); break;
            case Q: case LEFT:
                drone.getCommandManager().spinLeft(DroneController.getSpeed()); break;
            case E: case RIGHT:
                drone.getCommandManager().spinRight(DroneController.getSpeed()); break;
            case X: case UP:
                drone.up(); break;
            case Z: case DOWN:
                drone.down(); break;
            case SPACE:
                controller.takeoffOrLand();
                break;
            case ENTER:
                drone.getCommandManager().schedule(0, controller::flyThroughRing);
                break;
            case P:
                System.out.println("DRONE Altitude: " + controller.getDroneAltitude());
                System.out.println("DRONE Battery: " + controller.getDroneBattery());
                break;
            case BACK_SPACE:
                controller.stop();
                System.exit(0);
        }
    }
}

