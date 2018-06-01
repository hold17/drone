package dk.localghost.hold17.apps.cameracalibration;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.opencv.core.Core;

public class CameraCalibration extends Application {
    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
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
            controller.init();
            // show the GUI
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}