package dk.localghost.hold17.autonomous_drone.gui;

import dk.localghost.hold17.autonomous_drone.controller.DroneController;
import dk.localghost.hold17.autonomous_drone.opencv_processing.CircleFilter;
import dk.localghost.hold17.autonomous_drone.opencv_processing.FilterHelper;
import dk.localghost.hold17.base.IARDrone;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class GUIController {
    private IARDrone ardrone;
    private DroneController droneController;
    private static FilterHelper filterHelper = new FilterHelper();
    private CircleFilter circleFilter;
//    private static RectangleFilter rectangleFilter = new RectangleFilter();
    private Timer timer;
    private BufferedImage bufferedImage;

    @FXML
    private ImageView live;
    @FXML
    private ImageView filtered;
    @FXML
    private Slider h1_slider, s1_slider, v1_slider,
                   h2_slider, s2_slider, v2_slider,
                   h3_slider, s3_slider, v3_slider,
                   h4_slider, s4_slider, v4_slider;
    @FXML
    private Label h1_text, s1_text, v1_text,
                  h2_text, s2_text, v2_text,
                  h3_text, s3_text, v3_text,
                  h4_text, s4_text, v4_text;

    void init(IARDrone drone, DroneController droneController) {
        ardrone = drone;
        initSliders();
        startRecording();
        this.droneController = droneController;
        this. circleFilter = droneController.getCircleFilter();
    }

    @FXML
    private void startRecording() {
        bufferedImage = null;
//        ardrone.getCommandManager().setVideoChannel(VideoChannel.HORI);
//        ardrone.getCommandManager().setVideoCodec(VideoCodec.H264_720P);
//        ardrone.getVideoManager().reinitialize();

        ardrone.getVideoManager().addImageListener(newImage -> bufferedImage = newImage);

        TimerTask liveFrame = new TimerTask() {
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

        TimerTask processedFrame = new TimerTask() {
            @Override
            public void run() {
                if (bufferedImage != null) {
                    droneController.updateQR(bufferedImage);
                    final Mat mat = circleFilter.findCircleAndDraw(filterHelper.bufferedImageToMat(bufferedImage));
//                    droneController.alignCircle();
                    final BufferedImage bf = filterHelper.matToBufferedImage(mat);
                    final Image imageFiltered = SwingFXUtils.toFXImage(bf, null);
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
        this.timer.schedule(liveFrame, 0, 33);
        // update imageView with new image every 66ms (approx. 15 fps)
        this.timer.schedule(processedFrame, 0, 250);
    }

    /**
     * Generic method for putting element running on a non-JavaFX thread on the
     * JavaFX thread, to properly update the UI
     *
     * @param property
     *            a {@link ObjectProperty}
     * @param value
     *            the value to set for the given {@link ObjectProperty}
     */
    private static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> property.set(value));
    }

    private void initSliders() {
        h1_slider.setValue(circleFilter.getFilter1LowerBoundHue());
        h1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerBoundHue()));
        s1_slider.setValue(circleFilter.getFilter1LowerBoundSat());
        s1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerBoundSat()));
        v1_slider.setValue(circleFilter.getFilter1LowerBoundVal());
        v1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerBoundVal()));
        h2_slider.setValue(circleFilter.getFilter1UpperBoundHue());
        h2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperBoundHue()));
        s2_slider.setValue(circleFilter.getFilter1UpperBoundSat());
        s2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperBoundSat()));
        v2_slider.setValue(circleFilter.getFilter1UpperBoundVal());
        v2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperBoundVal()));
//        h3_slider.setValue(circleFilter.getH3());
//        h3_text.textProperty().setValue(String.valueOf(circleFilter.getH3()));
//        s3_slider.setValue(circleFilter.getS3());
//        s3_text.textProperty().setValue(String.valueOf(circleFilter.getS3()));
//        v3_slider.setValue(circleFilter.getV3());
//        v3_text.textProperty().setValue(String.valueOf(circleFilter.getV3()));
//        h4_slider.setValue(circleFilter.getH4());
//        h4_text.textProperty().setValue(String.valueOf(circleFilter.getH4()));
//        s4_slider.setValue(circleFilter.getS4());
//        s4_text.textProperty().setValue(String.valueOf(circleFilter.getS4()));
//        v4_slider.setValue(circleFilter.getV4());
//        v4_text.textProperty().setValue(String.valueOf(circleFilter.getV4()));
    }

    @FXML
    public void h1SliderUpdate() {
        final double h1_val = h1_slider.getValue();
        circleFilter.setFilter1LowerBoundHue(h1_val);
        h1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerBoundHue()));
    }

    @FXML
    public void s1SliderUpdate() {
        final double s1_val = s1_slider.getValue();
        circleFilter.setFilter1LowerBoundSat(s1_val);
        s1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerBoundSat()));
    }

    @FXML
    public void v1SliderUpdate() {
        final double v1_val = v1_slider.getValue();
        circleFilter.setFilter1LowerBoundVal(v1_val);
        v1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerBoundVal()));
    }

    @FXML
    public void h2SliderUpdate() {
        final double h2_val = h2_slider.getValue();
        circleFilter.setFilter1UpperBoundHue(h2_val);
        h2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperBoundHue()));
    }

    @FXML
    public void s2SliderUpdate() {
        final double s2_val = s2_slider.getValue();
        circleFilter.setFilter1UpperBoundSat(s2_val);
        s2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperBoundSat()));
    }

    @FXML
    public void v2SliderUpdate() {
        final double v2_val = v2_slider.getValue();
        circleFilter.setFilter1UpperBoundVal(v2_val);
        v2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperBoundVal()));
    }

}