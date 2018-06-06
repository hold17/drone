package dk.localghost.hold17.autonomous_drone.gui;

import dk.localghost.hold17.autonomous_drone.controller.DroneController;
import dk.localghost.hold17.autonomous_drone.controller.KeyboardCommandManager;
import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AutonomousGUI extends Application {
    private static IARDrone drone;
    private static String ip;

    private final static int SPEED = 20;

    private DroneController droneController;
    private KeyboardCommandManager keyboardManager;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("You must assign an ip address as argument.");
            System.exit(-1);
        }

        ip = args[0];

        AutonomousGUI autonomousGUI = new AutonomousGUI();
        autonomousGUI.initDrone();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AutonomousGUI.fxml"));
            BorderPane rootElement = loader.load();

            rootElement.setStyle("-fx-background-color: whitesmoke;");
            Scene scene = new Scene(rootElement, 1280, 720);
//            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            primaryStage.setTitle("Autonomous GUI for Hold 17");
            primaryStage.setScene(scene);

            GUIController controller = loader.getController();
            controller.init(drone);

            primaryStage.show();
//            primaryStage.setAlwaysOnTop(true);

            primaryStage.setOnCloseRequest(event -> {
                drone.stop();
                drone.disconnect();
            });

            // Add key listeners for the drone
            scene.addEventFilter(KeyEvent.KEY_PRESSED, keyboardManager);
            scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> droneController.hover());
        } catch (Exception e) {
            System.err.println("Couldn't load file: ");
            e.printStackTrace();
        }
    }

    private void initDrone() {
        drone = new ARDrone(ip);
        droneController = new DroneController(drone, SPEED);
        keyboardManager = new KeyboardCommandManager(droneController);
    }

}