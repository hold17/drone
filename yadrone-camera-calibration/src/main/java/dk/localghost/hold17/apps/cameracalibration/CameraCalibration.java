package dk.localghost.hold17.apps.cameracalibration;

import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javafx.stage.WindowEvent;
import org.opencv.core.Core;

public class CameraCalibration extends Application {
    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static IARDrone ardrone = null;

    public static void main(String[] args) {
        if (args[0] == null) {
            System.out.println("Drone IP not specified. Please launch with drone IP as first argument.");
            System.exit(-1);
        }
        ardrone = new ARDrone(args[0]);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CC_FX.fxml"));
            // store the root element so that the controllers can use it
            BorderPane rootElement = loader.load();
            // set a whitesmoke background
            rootElement.setStyle("-fx-background-color: whitesmoke;");
            // create and style a scene
            Scene scene = new Scene(rootElement, 1280, 720);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            // create the stage with the given title and the previously created
            // scene
            primaryStage.setTitle("Camera Calibration");
            primaryStage.setScene(scene);
            // init the controller variables
            CC_Controller controller = loader.getController();
            controller.init(ardrone);
            // show the GUI
            primaryStage.show();

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Platform.exit();
                    System.out.println("closing window");
                    System.exit(0);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}