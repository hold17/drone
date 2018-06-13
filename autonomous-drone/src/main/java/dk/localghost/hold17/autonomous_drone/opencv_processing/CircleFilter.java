package dk.localghost.hold17.autonomous_drone.opencv_processing;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.cvtColor;

// TODO: Clean up unused code
public class CircleFilter {

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

//    private final Scalar RED = new Scalar(0, 0, 255);
//    private final Scalar YELLOW = new Scalar(0, 255, 255);
//    private final Scalar CYAN = new Scalar(255, 255, 0);
//    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar HSV_NEON_GREEN = new Scalar(86, 240, 129);
    private Scalar HSV_LOWER_RED = new Scalar(0, 75, 50);
    private Scalar HSV_LOWISH_RED = new Scalar(14, 255, 200);
//    private Scalar HSV_UPPER_RED =  new Scalar(0, 0, 0);
//    private Scalar HSV_UPPERISH_RED = new Scalar(0, 0, 0);


    public Point getBiggestCircle() {
        return biggestCircle;
    }

    // Global variabel for centrum af største cirkel.
    private Point biggestCircle = new Point();

    public CircleFilter() {}

    /*
     * Funktion der finder cirkler samt tegner dem
     * TODO: Metode kan forveksle den største cirkel i scenarier hvor der er flere cirkler.
     */
    public Mat findCircleAndDraw(Mat image) {
        Mat circlePosition = new Mat();
        Mat hsv_image = new Mat();

        GaussianBlur(image, image, new Size(9, 9), 2, 2);
        cvtColor(image, hsv_image, Imgproc.COLOR_BGR2HSV);

        Mat lower_red = new Mat();
//        Mat upper_red = new Mat();

        Core.inRange(hsv_image, HSV_LOWER_RED, HSV_LOWISH_RED, lower_red);
//        Core.inRange(image, HSV_UPPER_RED, HSV_UPPERISH_RED, upper_red);

//        Mat red_hue_image = new Mat();
//        Core.addWeighted(lower_red, 1, upper_red, 1, 0, red_hue_image);

        // finder objekter der ligner cirkler og gemmer deres position i circlePosition
        // 4th argument = downscaling. Higher values = lower resolution. 1 = no downscaling.
        // 5th argument = minDist between circles
        Imgproc.HoughCircles(lower_red, circlePosition, Imgproc.CV_HOUGH_GRADIENT, 1, (lower_red.rows()/8), 100, 30, 100, 640);

        // fortsæt kun, hvis der er fundet én eller flere cirkler
        if (!circlePosition.empty()) {
            int maxRadius = 0;
            for (int i = 0; i < circlePosition.cols(); i++) { // antallet af kolonner angiver antallet af cirkler fundet
                double[] testArr = circlePosition.get(0, i);
                //System.out.println("\nCirkel nr. " + (i + 1) + " fundet på:\nx-koord: " + testArr[0] + "\ny-koord: " + testArr[1] + "\nradius: " + testArr[2]);
                // sætter cirklens centrum
                Point center = new Point(testArr[0], testArr[1]);

                // parser radius til int for at efterleve parametre krav i circle()
                double radiusDouble = testArr[2];
                int radius = (int) radiusDouble;
                // Vi ønsker kun at tegne den største cirkel
                if (maxRadius < radius) {
                    maxRadius = radius;
                    biggestCircle.x = center.x;
                    biggestCircle.y = center.y;
                }
            }
            // tegner cirklen
            Imgproc.circle(lower_red, biggestCircle, maxRadius, HSV_NEON_GREEN, 3, 8, 0);
        }
        Mat colorFiltered = new Mat();
        Core.bitwise_and(image, image, colorFiltered, lower_red);
        return colorFiltered;
    }



    public int getFilter1LowerBoundHue() {
        return (int) HSV_LOWER_RED.val[0];
    }

    public void setFilter1LowerBoundHue(double h1) {
        HSV_LOWER_RED.set(new double[]{h1, HSV_LOWER_RED.val[1], HSV_LOWER_RED.val[2]});
    }

    public int getFilter1LowerBoundSat() {
        return (int) HSV_LOWER_RED.val[1];
    }

    public void setFilter1LowerBoundSat(double s1) {
        HSV_LOWER_RED.set(new double[]{HSV_LOWER_RED.val[0], s1, HSV_LOWER_RED.val[2]});
    }

    public int getFilter1LowerBoundVal() {
        return (int) HSV_LOWER_RED.val[2];
    }

    public void setFilter1LowerBoundVal(double v1) {
        HSV_LOWER_RED.set(new double[]{HSV_LOWER_RED.val[0], HSV_LOWER_RED.val[1], v1});
    }

    public int getFilter1UpperBoundHue() {
        return (int) HSV_LOWISH_RED.val[0];
    }

    public void setFilter1UpperBoundHue(double h2) {
        HSV_LOWISH_RED.set(new double[]{h2, HSV_LOWISH_RED.val[1], HSV_LOWISH_RED.val[2]});
    }

    public int getFilter1UpperBoundSat() {
        return (int) HSV_LOWISH_RED.val[1];
    }

    public void setFilter1UpperBoundSat(double s2) {
        HSV_LOWISH_RED.set(new double[]{HSV_LOWISH_RED.val[0], s2, HSV_LOWISH_RED.val[2]});
    }

    public int getFilter1UpperBoundVal() {
        return (int) HSV_LOWISH_RED.val[2];
    }

    public void setFilter1UpperBoundVal(double v2) {
        HSV_LOWISH_RED.set(new double[]{HSV_LOWISH_RED.val[0], HSV_LOWISH_RED.val[1], v2});
    }

    public int getH3() {
        return (int) HSV_LOWER_RED.val[0];
    }

    public void setH3(double h3) {
        HSV_LOWER_RED.set(new double[]{h3, HSV_LOWER_RED.val[1], HSV_LOWER_RED.val[2]});
    }

    public int getS3() {
        return (int) HSV_LOWER_RED.val[1];
    }

    public void setS3(double s3) {
        HSV_LOWER_RED.set(new double[]{HSV_LOWER_RED.val[0], s3, HSV_LOWER_RED.val[2]});
    }

    public int getV3() {
        return (int) HSV_LOWER_RED.val[2];
    }

    public void setV3(double v3) {
        HSV_LOWER_RED.set(new double[]{HSV_LOWER_RED.val[0], HSV_LOWER_RED.val[3], v3});
    }

    public int getH4() {
        return (int) HSV_LOWISH_RED.val[0];
    }

    public void setH4(double h4) {
        HSV_LOWISH_RED.set(new double[]{h4, HSV_LOWISH_RED.val[1], HSV_LOWISH_RED.val[2]});
    }

    public int getS4() {
        return (int) HSV_LOWISH_RED.val[1];
    }

    public void setS4(double s4) {
        HSV_LOWISH_RED.set(new double[]{HSV_LOWISH_RED.val[0], s4, HSV_LOWISH_RED.val[2]});
    }

    public int getV4() {
        return (int) HSV_LOWISH_RED.val[2];
    }

    public void setV4(double v4) {
        HSV_LOWISH_RED.set(new double[]{HSV_LOWISH_RED.val[0], HSV_LOWISH_RED.val[1], v4});
    }
}