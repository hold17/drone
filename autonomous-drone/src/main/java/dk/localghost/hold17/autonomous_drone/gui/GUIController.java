package dk.localghost.hold17.autonomous_drone.gui;

import dk.localghost.hold17.autonomous_drone.controller.DroneController;
import dk.localghost.hold17.autonomous_drone.opencv_processing.FilterHelper;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.utils.ConsoleColors;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class GUIController {
    private IARDrone ardrone;
    private DroneController droneController;

    private Timer timer;
    private BufferedImage bufferedImage;

    @FXML
    private ImageView live;
    @FXML
    private ImageView filtered;

    private static FilterHelper filterHelper = new FilterHelper();
//    private static RectangleFilter rectangleFilter = new RectangleFilter();

    void init(IARDrone drone, DroneController droneController) {
        ardrone = drone;
        startRecording();
        this.droneController = droneController;
    }

    private void startRecording() {
        bufferedImage = null;
//        ardrone.getCommandManager().setVideoChannel(VideoChannel.HORI);
//        ardrone.getCommandManager().setVideoCodec(VideoCodec.H264_720P);
//        ardrone.getVideoManager().reinitialize();

        ardrone.getVideoManager().addImageListener(newImage -> bufferedImage = newImage);

        TimerTask frameGrabber = new TimerTask() {
            @Override
            public void run() {
                if (bufferedImage != null) {
                    Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                    // show the image
                    Platform.runLater(() -> {
                        live.setImage(image);
                        // set fixed width
                        live.setFitWidth(640);
                        // preserve bufferedImage ratio
                        live.setPreserveRatio(true);
                    });

                } else {
                    // System.out.println("bufferedImage was null"); // SILENCED UNTIL NEEDED
                }
            }
        };

        TimerTask processImage = new TimerTask() {
            @Override
            public void run() {
                if (bufferedImage != null) {
                    droneController.updateQR(bufferedImage);
                    Mat mat = droneController.getCircleFilter().findCircleAndDraw(filterHelper.bufferedImageToMat(bufferedImage));
//                    droneController.alignCircle();
                    BufferedImage bf = filterHelper.matToBufferedImage(mat);
                    Image imageFiltered = SwingFXUtils.toFXImage(bf, null);
                    Platform.runLater(() -> {
                        filtered.setImage(imageFiltered);
                        filtered.setFitWidth(640);
                        filtered.setPreserveRatio(true);
                    });
                }
            }
        };

        this.timer = new Timer();
        // update imageView with new image every 33ms (30 fps)
        this.timer.schedule(frameGrabber, 0, 33);
        this.timer.schedule(processImage, 0, 1000);
    }
}
