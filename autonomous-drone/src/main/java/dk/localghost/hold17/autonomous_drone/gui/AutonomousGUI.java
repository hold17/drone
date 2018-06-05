package dk.localghost.hold17.autonomous_drone.gui;

import dk.localghost.hold17.autonomous_drone.controller.DroneController;
import dk.localghost.hold17.autonomous_drone.controller.KeyboardCommandManager;
import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;

public class AutonomousGUI extends Application {
    private IARDrone drone;
    private static String ip;

    private final static int SPEED = 30;

    private DroneController droneController;
    private KeyboardCommandManager keyboardManager;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("You must assign an ip address as argument.");
            System.exit(-1);
        }

        ip = args[0];

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        drone = new ARDrone(ip);
        droneController = new DroneController(drone, SPEED);
        keyboardManager = new KeyboardCommandManager(droneController);

        try {
            URL url = getClass().getResource("AutonomousGUI.fxml");
            System.out.println("URL: " + url.toString());

            Parent root = FXMLLoader.load(url);
            primaryStage.setTitle("Autonomous GUI for Hold 17");
            Scene scene = new Scene(root, 300, 200);
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setAlwaysOnTop(true);

            // Add key listeners for the drone
            scene.addEventFilter(KeyEvent.KEY_PRESSED, keyboardManager);
            scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> droneController.hover());
        } catch (Exception e) {
            System.err.println("Couldn't load file: ");
            e.printStackTrace();
        }
    }
}
