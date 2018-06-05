package dk.localghost.hold17.autonomous_drone;

import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.LEDAnimation;
import dk.localghost.hold17.base.navdata.Altitude;
import dk.localghost.hold17.base.navdata.AltitudeListener;
import dk.localghost.hold17.base.navdata.BatteryListener;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;

public class AutonomousUI extends Application {
    private IARDrone drone;
    private String currentKey = "";

    private int droneAltitude = 0;
    private int droneBattery = 0;

    private boolean droneFlying = false;

    private final static int MAX_ALTITUDE = 2000;
    private final static int MIN_ALTITUDE = 1000;
    private final static int SPEED = 30;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        drone = new ARDrone("10.0.1.2");
        drone.start();
        drone.getCommandManager().flatTrim();

        drone.getNavDataManager().addAltitudeListener(new AltitudeListener() {
            @Override
            public void receivedAltitude(int altitude) {
                droneAltitude = altitude;
            }

            @Override
            public void receivedExtendedAltitude(Altitude d) {

            }
        });
        drone.getNavDataManager().addBatteryListener(new BatteryListener() {
            @Override
            public void batteryLevelChanged(int percentage) {
                droneBattery = percentage;
            }

            @Override
            public void voltageChanged(int vbat_raw) {

            }
        });

        drone.setMinAltitude(MIN_ALTITUDE);
        drone.setMaxAltitude(MAX_ALTITUDE);

        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 1);

        try {
            URL url = getClass().getResource("stuff.fxml");
            System.out.println("URL: " + url.toString());

            Parent root = FXMLLoader.load(url);
            primaryStage.setTitle("Hej Justin Fabricius, du stinker!");
            Scene scene = new Scene(root, 300, 200);
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setAlwaysOnTop(true);

            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                switch(event.getCode()) {
                    case TAB:
                        drone.reset(); // No break is intentional here
                    case B:
                        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 1);
                        break;
                    case T:
                        drone.takeOff(); break;
                    case G:
                        drone.landing(); break;
                    case W:
                        drone.forward(); break;
                    case S:
                        drone.backward(); break;
                    case A:
                        drone.goLeft(); break;
                    case D:
                        drone.goRight(); break;
                    case Q: case LEFT:
                        drone.getCommandManager().spinLeft(SPEED); break;
                    case E: case RIGHT:
                        drone.getCommandManager().spinRight(SPEED); break;
                    case X: case UP:
                        drone.up(); break;
                    case Z: case DOWN:
                        drone.down(); break;
                    case SPACE:
                        if (droneFlying) {
                            drone.landing();
                            droneFlying = false;
                        } else {
                            drone.takeOff();
                            droneFlying = true;
                        }
                        break;
                    case ENTER:
//                        drone.getCommandManager().schedule(0, this::flyThroughRing);
                        drone.getCommandManager().spinRight(SPEED).doFor(2000);
                        break;
                    case P:
                        System.out.println("DRONE Altitude: " + droneAltitude);
                        System.out.println("DRONE Battery: " + droneBattery);
                        break;
                    case BACK_SPACE:
                        drone.stop();
                        System.exit(0);
                }
            });

            scene.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    drone.hover();
                }
            });
        } catch (Exception e) {
            System.err.println("Couldn't load file: ");
            e.printStackTrace();
        }
    }

    private void flyThroughRing() {
        System.out.println("          FLYING UP");
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 1);
        while(droneAltitude < MAX_ALTITUDE - 200) {
            drone.getCommandManager().up(100).doFor(500);
        }

        drone.getCommandManager().hover().doFor(250);

        System.out.println("          FLYING FORWARD");
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 1);
        drone.getCommandManager().forward(SPEED).doFor(1000);

        drone.getCommandManager().hover().doFor(250);

        System.out.println("          FLYING DOWN");
        while(droneAltitude > MIN_ALTITUDE) {
            drone.getCommandManager().down(SPEED).doFor(500);
        }

        drone.getCommandManager().hover().doFor(250);

        drone.getCommandManager().backward(SPEED).doFor(1000);
        drone.getCommandManager().hover();
    }
}
