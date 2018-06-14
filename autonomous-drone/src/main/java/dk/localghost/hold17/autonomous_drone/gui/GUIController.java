package dk.localghost.hold17.autonomous_drone.gui;

import dk.localghost.hold17.autonomous_drone.controller.DroneController;
import dk.localghost.hold17.autonomous_drone.opencv_processing.CircleFilter;
import dk.localghost.hold17.autonomous_drone.opencv_processing.FilterHelper;
import dk.localghost.hold17.autonomous_drone.opencv_processing.RectangleFilter;
import dk.localghost.hold17.base.IARDrone;
import javafx.application.Platform;
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
    private RectangleFilter rectangleFilter;
//    private static RectangleFilter rectangleFilter = new RectangleFilter();
    private Timer timer;
    private BufferedImage bufferedImage;

    @FXML
    private ImageView live;
    @FXML
    private ImageView filteredImgCircle;
    @FXML
    private ImageView filteredImgRectangle;
    @FXML
    private Slider h1_slider, s1_slider, v1_slider,
                   h2_slider, s2_slider, v2_slider,
                   h3_slider, s3_slider, v3_slider,
                   h4_slider, s4_slider, v4_slider,
                   param1_slider, param2_slider;
    @FXML
    private Label h1_text, s1_text, v1_text,
                  h2_text, s2_text, v2_text,
                  h3_text, s3_text, v3_text,
                  h4_text, s4_text, v4_text,
                  param1_text, param2_text;

    void init(IARDrone drone, DroneController droneController) {
        this.ardrone = drone;
        this.droneController = droneController;
        this.circleFilter = droneController.getCircleFilter();
        this.droneController.getRectangleFilter();

        initSliders();
        startRecording();
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
//                        live.setFitWidth(640);
                        // preserve bufferedImage ratio
//                        live.setPreserveRatio(true);
                    });
                } else {
                   // System.out.println("bufferedImage was null"); // SILENCED UNTIL NEEDED
                }
            }
        };

        TimerTask processedCircleFrame = new TimerTask() {
            @Override
            public void run() {
                if (bufferedImage != null) {
                    droneController.updateQR(bufferedImage);
                    final Mat mat = circleFilter.findCircleAndDraw(filterHelper.bufferedImageToMat(bufferedImage));
//                    droneController.alignCircle();
                    final BufferedImage bf = filterHelper.matToBufferedImage(mat);
                    final Image imageFiltered = SwingFXUtils.toFXImage(bf, null);
                    Platform.runLater(() -> {
                        filteredImgCircle.setImage(imageFiltered);
//                        filteredImgCircle.setFitWidth(640);
//                        filteredImgCircle.setPreserveRatio(true);
                    });
                }
            }
        };
        TimerTask processedRectangleFrame = new TimerTask() {
            @Override
            public void run() {
                if (bufferedImage != null) {
                    final Mat mat = rectangleFilter.filterImage(bufferedImage);
//                    droneController.alignCircle();
                    final BufferedImage bf = filterHelper.matToBufferedImage(mat);
                    final Image imageFiltered = SwingFXUtils.toFXImage(bf, null);
                    Platform.runLater(() -> {
                        filteredImgRectangle.setImage(imageFiltered);
//                        filteredImgCircle.setFitWidth(640);
//                        filteredImgCircle.setPreserveRatio(true);
                    });
                }
            }
        };

        this.timer = new Timer();
        // update imageView with new image every 33ms (30 fps)
        this.timer.schedule(liveFrame, 0, 33);
        // update imageView with new image every 66ms (approx. 15 fps)
        this.timer.schedule(processedCircleFrame, 0, 66);
        this.timer.schedule(processedRectangleFrame, 0, 66);
    }

    private void initSliders() {
        h1_slider.setValue(circleFilter.getFilter1LowerHue());
        h1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerHue()));
        s1_slider.setValue(circleFilter.getFilter1LowerSat());
        s1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerSat()));
        v1_slider.setValue(circleFilter.getFilter1LowerVal());
        v1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerVal()));

        h2_slider.setValue(circleFilter.getFilter1UpperHue());
        h2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperHue()));
        s2_slider.setValue(circleFilter.getFilter1UpperSat());
        s2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperSat()));
        v2_slider.setValue(circleFilter.getFilter1UpperVal());
        v2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperVal()));

        h3_slider.setValue(circleFilter.getFilter2LowerHue());
        h3_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2LowerHue()));
        s3_slider.setValue(circleFilter.getFilter2LowerSat());
        s3_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2LowerSat()));
        v3_slider.setValue(circleFilter.getFilter2LowerVal());
        v3_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2LowerVal()));

        h4_slider.setValue(circleFilter.getFilter2UpperHue());
        h4_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2UpperHue()));
        s4_slider.setValue(circleFilter.getFilter2UpperSat());
        s4_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2UpperSat()));
        v4_slider.setValue(circleFilter.getFilter2UpperVal());
        v4_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2UpperVal()));

        param1_slider.setValue(circleFilter.getParam1());
        param1_text.textProperty().setValue(String.valueOf(circleFilter.getParam1()));

        param2_slider.setValue(circleFilter.getParam2());
        param2_text.textProperty().setValue(String.valueOf(circleFilter.getParam2()));
    }

    @FXML
    public void h1SliderUpdate() {
        final double h1_val = h1_slider.getValue();
        circleFilter.setFilter1LowerHue(h1_val);
        h1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerHue()));
    }

    @FXML
    public void s1SliderUpdate() {
        final double s1_val = s1_slider.getValue();
        circleFilter.setFilter1LowerSat(s1_val);
        s1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerSat()));
    }

    @FXML
    public void v1SliderUpdate() {
        final double v1_val = v1_slider.getValue();
        circleFilter.setFilter1LowerVal(v1_val);
        v1_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1LowerVal()));
    }

    @FXML
    public void h2SliderUpdate() {
        final double h2_val = h2_slider.getValue();
        circleFilter.setFilter1UpperHue(h2_val);
        h2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperHue()));
    }

    @FXML
    public void s2SliderUpdate() {
        final double s2_val = s2_slider.getValue();
        circleFilter.setFilter1UpperSat(s2_val);
        s2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperSat()));
    }

    @FXML
    public void v2SliderUpdate() {
        final double v2_val = v2_slider.getValue();
        circleFilter.setFilter1UpperVal(v2_val);
        v2_text.textProperty().setValue(String.valueOf(circleFilter.getFilter1UpperVal()));
    }

    @FXML
    public void h3SliderUpdate() {
        final double h3_val = h3_slider.getValue();
        circleFilter.setFilter2LowerHue(h3_val);
        h3_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2LowerHue()));
    }

    @FXML
    public void s3SliderUpdate() {
        final double s3_val = s3_slider.getValue();
        circleFilter.setFilter2LowerSat(s3_val);
        s3_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2LowerSat()));
    }

    @FXML
    public void v3SliderUpdate() {
        final double v3_val = v3_slider.getValue();
        circleFilter.setFilter2LowerVal(v3_val);
        v3_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2LowerVal()));
    }

    @FXML
    public void h4SliderUpdate() {
        final double h4_val = h4_slider.getValue();
        circleFilter.setFilter2UpperHue(h4_val);
        h4_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2UpperHue()));
    }

    @FXML
    public void s4SliderUpdate() {
        final double s4_val = s4_slider.getValue();
        circleFilter.setFilter2UpperSat(s4_val);
        s4_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2UpperSat()));
    }

    @FXML
    public void v4SliderUpdate() {
        final double v4_val = v4_slider.getValue();
        circleFilter.setFilter2UpperVal(v4_val);
        v4_text.textProperty().setValue(String.valueOf(circleFilter.getFilter2UpperVal()));
    }

    @FXML
    public void param1SliderUpdate() {
        final double param1_val = param1_slider.getValue();
        circleFilter.setParam1((int) param1_val);
        param1_text.textProperty().setValue(String.valueOf(circleFilter.getParam1()));
    }

    @FXML
    public void param2SliderUpdate() {
        final double param2_val = param2_slider.getValue();
        circleFilter.setParam2((int) param2_val);
        param2_text.textProperty().setValue(String.valueOf(circleFilter.getParam2()));
    }

}