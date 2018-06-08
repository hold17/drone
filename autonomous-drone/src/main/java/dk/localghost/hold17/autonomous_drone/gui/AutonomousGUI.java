package dk.localghost.hold17.autonomous_drone.gui;

import dk.localghost.hold17.autonomous_drone.controller.DroneController;
import dk.localghost.hold17.autonomous_drone.controller.KeyboardCommandManager;
import dk.localghost.hold17.autonomous_drone.controller.QRCodeScanner;
import dk.localghost.hold17.autonomous_drone.controller.QRScannerController;
import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AutonomousGUI extends Application {
    private static IARDrone drone;
    private final static int SPEED = 20;
    private DroneController droneController;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("You must assign an ip address as argument.");
            System.exit(-1);
        }
        drone = new ARDrone(args[0]);
        drone.start();

        QRCodeScanner qrScanner = new QRCodeScanner();
        drone.getVideoManager().addImageListener(qrScanner::imageUpdated);
        qrScanner.addListener(new QRScannerController());


        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AutonomousGUI.fxml"));
            BorderPane rootElement = loader.load();
            Scene scene = new Scene(rootElement, 1280, 720);
            primaryStage.setTitle("Autonomous GUI for Hold 17");
            primaryStage.setScene(scene);

            droneController = new DroneController(drone, SPEED);
            KeyboardCommandManager keyboardManager = new KeyboardCommandManager(droneController);

            GUIController controller = loader.getController();
            controller.init(drone);

            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                drone.stop();
                drone.disconnect();
                System.out.println("User closed the window");
                System.exit(0);
            });

            // Add key listeners for the drone
            scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> droneController.hover());
            scene.addEventFilter(KeyEvent.KEY_PRESSED, keyboardManager);
        } catch (Exception e) {
            System.err.println("Couldn't load file: ");
            e.printStackTrace();
        }
    }

}