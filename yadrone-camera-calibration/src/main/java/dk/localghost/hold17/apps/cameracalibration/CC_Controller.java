package dk.localghost.hold17.apps.cameracalibration;

import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.VideoChannel;
import dk.localghost.hold17.base.command.VideoCodec;
import dk.localghost.hold17.base.video.ImageListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CC_Controller {
    // FXML buttons
    @FXML
    private Button cameraButton;
    @FXML
    private Button applyButton;
    @FXML
    private Button snapshotButton;
    // the FXML area for showing the current frame (before calibration)
    @FXML
    private ImageView originalFrame;
    // the FXML area for showing the current frame (after calibration)
    @FXML
    private ImageView calibratedFrame;
    // info related to the calibration process
    @FXML
    private TextField numBoards;
    @FXML
    private TextField numHorCorners;
    @FXML
    private TextField numVerCorners;
    // a timer for acquiring the video stream
    private Timer timer;
    // a flag to change the button behavior
    private boolean cameraActive;
    // the saved chessboard image
    private Mat savedImage;
    // the calibrated camera frame
    private Image undistortedImage, CamStream;
    // various variables needed for the calibration
    private List<Mat> imagePoints;
    private List<Mat> objectPoints;
    private MatOfPoint3f obj;
    private MatOfPoint2f imageCorners;
    private int boardsNumber;
    private int numCornersHor;
    private int numCornersVer;
    private int successes;
    private Mat intrinsic;
    private Mat distCoeffs;
    private boolean isCalibrated;
    private BufferedImage image;
    private IARDrone ardrone = null;

    /**
     * Init all the (global) variables needed in the controller
     */
    protected void init(IARDrone drone) {
        this.cameraActive = false;
        this.obj = new MatOfPoint3f();
        this.imageCorners = new MatOfPoint2f();
        this.savedImage = new Mat();
        this.undistortedImage = null;
        this.imagePoints = new ArrayList<>();
        this.objectPoints = new ArrayList<>();
        this.intrinsic = new Mat(3, 3, CvType.CV_32FC1);
        this.distCoeffs = new Mat();
        this.successes = 0;
        this.isCalibrated = false;
        this.snapshotButton.setText("Take snapshot (" + (this.successes+1) + " of " + this.numBoards.getText() + ")");

        ardrone = drone;
        ardrone.start();
    }

    /**
     * Store all the chessboard properties, update the UI and prepare other needed variables
     */
    @FXML
    protected void updateSettings() {
        this.boardsNumber = Integer.parseInt(this.numBoards.getText());
        this.numCornersHor = Integer.parseInt(this.numHorCorners.getText());
        this.numCornersVer = Integer.parseInt(this.numVerCorners.getText());
        int numSquares = this.numCornersHor * this.numCornersVer;
        for (int j = 0; j < numSquares; j++)
            obj.push_back(new MatOfPoint3f(new Point3(j / this.numCornersHor, j % this.numCornersVer, 0.0f)));
        this.cameraButton.setDisable(false);
    }

    /**
     * The action triggered by pushing the button on the GUI
     */
    @FXML
    protected void startRecording() {
        image = null;
        if (!this.cameraActive) {
            this.cameraActive = true;
            ardrone.getCommandManager().setVideoChannel(VideoChannel.HORI);
            ardrone.getCommandManager().setVideoCodec(VideoCodec.H264_720P);
            ardrone.getVideoManager().reinitialize();

            ardrone.getVideoManager().addImageListener(new ImageListener() {
                @Override
                public void imageUpdated(BufferedImage newImage) {
                    image = newImage;
//                    System.out.println("image updated");
                }
            });

            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // grab a frame every 33 ms (30 frames/sec)
            TimerTask frameGrabber = new TimerTask() {
                @Override
                public void run() {
                    if (image != null) {
//                        System.out.println("this worked");
                        CamStream = grabFrame();
                        // show the original frames
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                originalFrame.setImage(CamStream);
                                // set fixed width
                                originalFrame.setFitWidth(640);
                                // preserve image ratio
                                originalFrame.setPreserveRatio(true);
                                // show the original frames
                                calibratedFrame.setImage(undistortedImage);
                                // set fixed width
                                calibratedFrame.setFitWidth(640);
                                // preserve image ratio
                                calibratedFrame.setPreserveRatio(true);
                            }
                        });
                    } else {
                        System.out.println("image was null");
                    }
                }

            };
            this.timer = new Timer();
            this.timer.schedule(frameGrabber, 0, 33);

            // update the button content
            this.cameraButton.setText("Stop Camera");
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update the button content again
            this.cameraButton.setText("Start Camera");
            // stop the timer
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }

            ardrone.restart();

            // clean the image areas
            originalFrame.setImage(null);
            calibratedFrame.setImage(null);
        }

    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Image} to show
     */
    private Image grabFrame() {
        // init everything
        Image imageToShow = null;
        Mat frame;
        try {
            // read the current frame
            frame = bufferedImageToMat(image);

            // if the frame is not empty, process it
            if (!frame.empty()) {
                // show the chessboard pattern
                // perform this operation only before starting the calibration process
                 if (this.successes < this.boardsNumber)
                     this.findAndDrawPoints(frame);

                 if (this.isCalibrated) {
                     // prepare the undistorted image
                     Mat undistorted = new Mat();
                     Imgproc.undistort(frame, undistorted, intrinsic, distCoeffs);
                     undistortedImage = mat2Image(undistorted);
                 }
                 // convert the Mat object (OpenCV) to Image (JavaFX)
                 imageToShow = mat2Image(frame);
            }

        } catch (IOException e) {
            // log the (full) error
            System.err.print("ERROR");
            e.printStackTrace();
        }

        return imageToShow;
    }

    /**
     * Take a snapshot to be used for the calibration process
     */
    @FXML
    protected void takeSnapshot() {
        if (this.successes < this.boardsNumber) {
            // save all the needed values
            this.imagePoints.add(imageCorners);
            imageCorners = new MatOfPoint2f();
            this.objectPoints.add(obj);
            this.successes++;
            this.snapshotButton.setText("Take snapshot (" + (this.successes + 1) + " of " + this.numBoards.getText() + ")");
        }

        // reach the correct number of images needed for the calibration
        if (this.successes == this.boardsNumber) {
            this.calibrateCamera();
        }
    }

    /**
     * Find and draws the points needed for the calibration on the chessboard
     *
     * @param frame the current frame
     */
    private void findAndDrawPoints(Mat frame) {
        // init
        Mat grayImage = new Mat();
        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);
        // the size of the chessboard
        Size boardSize = new Size(this.numCornersHor, this.numCornersVer);
        // look for the inner chessboard corners
        //imageCorners = new MatOfPoint2f();
//        System.out.println("1." + imageCorners.toString());
        boolean found = Calib3d.findChessboardCorners(grayImage, boardSize, imageCorners,
                Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
//        System.out.println("2." + imageCorners.toString() + " || " + found);
        // all the required corners have been found...
        if (found && imageCorners.isContinuous()) {
            // optimization
            TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
            Imgproc.cornerSubPix(grayImage, imageCorners, new Size(11, 11), new Size(-1, -1), term);
            // save the current frame for further elaborations
            grayImage.copyTo(this.savedImage);
            // show the chessboard inner corners on screen
            Calib3d.drawChessboardCorners(frame, boardSize, imageCorners, found);

            // enable the option for taking a snapshot
            this.snapshotButton.setDisable(false);
        } else {
            this.snapshotButton.setDisable(true);
        }
    }

    /**
     * The effective camera calibration, to be performed once in the program
     * execution
     */
    private void calibrateCamera() {
        // init needed variables according to OpenCV docs
        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();
        intrinsic.put(0, 0, 1);
        intrinsic.put(1, 1, 1);
        // calibrate!
        Calib3d.calibrateCamera(objectPoints, imagePoints, savedImage.size(), intrinsic, distCoeffs, rvecs, tvecs);
        this.isCalibrated = true;

        // you cannot take other snapshot, at this point...
        this.snapshotButton.setDisable(true);
    }

    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    private Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public Mat bufferedImageToMat(BufferedImage img) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", outstream);
        outstream.flush();
        return Imgcodecs.imdecode(new MatOfByte(outstream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

}