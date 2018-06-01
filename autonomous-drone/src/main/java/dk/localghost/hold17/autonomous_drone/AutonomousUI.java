package dk.localghost.hold17.autonomous_drone;


import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;

public class AutonomousUI extends Application {
    private IARDrone drone;
    private String currentKey = "";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//        drone = new ARDrone("10.0.1.2");
//        drone.start();
//        drone.getCommandManager().flatTrim();

        try {
            URL url = getClass().getResource("stuff.fxml");
            System.out.println("URL: " + url.toString());

            Parent root = FXMLLoader.load(url);
            primaryStage.setTitle("Hej Justin Fabricius, du stinker!");
            Scene scene = new Scene(root, 300, 200);
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setAlwaysOnTop(true);

            scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    switch(event.getCode()) {
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
                            drone.spinLeft(); break;
                        case E: case RIGHT:
                            drone.spinRight(); break;
                        case X: case UP:
                            drone.up(); break;
                        case Z: case DOWN:
                            drone.down(); break;
                    }
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
}
