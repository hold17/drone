package dk.localghost.hold17.autonomous_drone.controller;

import dk.localghost.hold17.base.IARDrone;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class KeyboardCommandManager implements EventHandler<KeyEvent> {
    private DroneController controller;
    private IARDrone drone;
    private static final int MANUAL_FLIGHT_SPEED = 30;

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
                drone.getCommandManager().forward(MANUAL_FLIGHT_SPEED); break;
            case S:
                drone.getCommandManager().backward(MANUAL_FLIGHT_SPEED); break;
            case A:
                drone.getCommandManager().goLeft(MANUAL_FLIGHT_SPEED); break;
            case D:
                drone.getCommandManager().goRight(MANUAL_FLIGHT_SPEED); break;
            case Q: case LEFT:
                drone.getCommandManager().spinLeft(50); break;
            case E: case RIGHT:
                drone.getCommandManager().spinRight(50); break;
            case UP:
                drone.up(); break;
            case DOWN:
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
            case M:
                drone.restart(); break;
            case BACK_SPACE:
                controller.stop();
                System.exit(0);
                break;
            case X:
//                controller.bum();
                drone.getCommandManager().schedule(0, controller::alignQrCode);
                break;
        }
    }
}

