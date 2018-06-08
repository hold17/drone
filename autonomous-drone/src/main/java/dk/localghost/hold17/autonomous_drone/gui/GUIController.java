package dk.localghost.hold17.autonomous_drone.gui;

import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.VideoChannel;
import dk.localghost.hold17.base.command.VideoCodec;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class GUIController {
    private IARDrone ardrone;
    private Timer timer;
    private BufferedImage bufferedImage;

    @FXML
    private ImageView cameraView;

    void init(IARDrone drone) {
        ardrone = drone;
        startRecording();
    }

    private void startRecording() {
        bufferedImage = null;
        ardrone.getCommandManager().setVideoChannel(VideoChannel.HORI);
        ardrone.getCommandManager().setVideoCodec(VideoCodec.H264_720P);
        ardrone.getVideoManager().reinitialize();

        ardrone.getVideoManager().addImageListener(newImage -> bufferedImage = newImage);

        TimerTask frameGrabber = new TimerTask() {
            @Override
            public void run() {
                if (bufferedImage != null) {
                    Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                    // show the image
                    Platform.runLater(() -> {
                        cameraView.setImage(image);
                        // set fixed width
                        cameraView.setFitWidth(1280);
                        // preserve bufferedImage ratio
                        cameraView.setPreserveRatio(true);
                    });
                } else {
                   // System.out.println("bufferedImage was null"); // SILENCED UNTIL NEEDED
                }
            }
        };
        this.timer = new Timer();
        // update imageView with new image every 33ms (30 fps)
        this.timer.schedule(frameGrabber, 0, 33);
    }
}