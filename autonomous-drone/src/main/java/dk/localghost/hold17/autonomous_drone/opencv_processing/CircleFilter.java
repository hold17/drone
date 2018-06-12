package dk.localghost.hold17.autonomous_drone.opencv_processing;

import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.*;

// TODO: Clean up unused code
public class CircleFilter {

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar HSV_NEON_GREEN = new Scalar(86, 240, 129);
    private final Scalar RED = new Scalar(0, 0, 255);
    private Scalar HSV_LOWER_RED = new Scalar(0, 75, 50);
    private Scalar HSV_LOWISH_RED = new Scalar(14, 255, 200);
    private Scalar HSV_UPPER_RED =  new Scalar(0, 0, 0);
    private Scalar HSV_UPPERISH_RED = new Scalar(0, 0, 0);

    private final Scalar YELLOW = new Scalar(0, 255, 255);
    private final Scalar CYAN = new Scalar(255, 255, 0);

    // Global variabel for centrum af største cirkel.
    private Point biggestCircle = new Point();

    public CircleFilter() {
//        BufferedImage img = matToBufferedImage(openFile(fileName));
//        Mat img_circle = openFile("3.jpg");
//        findCircleAndDraw(img_circle);
//        Direction direction = findDirectionFromCircle(biggestCircle);
//        System.out.println(direction);
//        saveFile(outputName, img_circle);
    }

//    public static void main(String[] args) {
//        new CircleFilter();
//    }

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
        Mat upper_red = new Mat();

//        Core.extractChannel(image, lower_red, 2);
//        Core.extractChannel(image, lower_red, 2);

        Core.inRange(hsv_image, HSV_LOWER_RED, HSV_LOWISH_RED, lower_red);
//        Core.inRange(image, HSV_UPPER_RED, HSV_UPPERISH_RED, upper_red);

        Mat red_hue_image = new Mat();
//        Core.addWeighted(lower_red, 1, upper_red, 1, 0, red_hue_image);



        // 4th argument = downscaling. Higher values = lower resolution. 1 = no downscaling.
        // 5th argument = minDist between circles
        Imgproc.HoughCircles(red_hue_image, circlePosition, Imgproc.CV_HOUGH_GRADIENT, 1, (red_hue_image.rows()/8), 100, 50, 100, 1280);

        // finder objekter der ligner cirkler og gemmer deres position i circlePosition
        // fortsæt kun, hvis der er fundet én eller flere cirkler
        if (circlePosition.empty()) {
            System.out.println("No circles found!");
        }
//        Point maxCenter;
        //System.out.println("Fandt: " + circlePosition.cols() + " cirkler");

        // sætter cirklens farve
//            Scalar color = new Scalar(100);

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
//        cvtColor(colorFiltered, colorFiltered, COLOR_GRAY2BGR);
        Imgproc.circle(red_hue_image, biggestCircle, maxRadius, HSV_NEON_GREEN, 3, 8, 0);
        Mat colorFiltered = new Mat();
        Core.bitwise_and(image, image, colorFiltered, lower_red);
        return colorFiltered;
    }

    public Direction findDirectionFromCircle(Point circleCoordinate) {
        if (circleCoordinate == null) {
            System.out.println("Point not initialized");
            return Direction.UNKNOWN;
        } else {
            double x = circleCoordinate.x;
            if (x > 0 && x < 512) return Direction.LEFT;
            else if (x > 512 && x < 768) return Direction.CENTER; // 256px (1/5 af billedeopløsningen på 1280)
            else if (x > 768 && x < 1280) return Direction.RIGHT;
            else {
                return Direction.UNKNOWN;
            }
        }
    }

    public Direction findDirectionFromCircleGrid(Point circleCoordinate) {
        if (circleCoordinate == null) {
            System.out.println("Point not initialized");
            return Direction.UNKNOWN;
        } else {
            double x = circleCoordinate.x;
            double y = circleCoordinate.y;
            return Direction.exactDirection(x, y);
        }
    }

    /*
     * Ikke færdig.
     *
     * Skal finde cirkel, lige nu forsøges med diverse metoder for at se hvad virker bedst
     */
//    Mat FindCircle_v2(Mat img){
//        //Sorterer vise farver fra, så vi ender med tydeligere cirkler
//        img = detectEdgesThreshold(img); // from javacv... find opencv alternative
//
//        // Fjerner farver, så vi kun har røde farver tilbage
//        Scalar lower_red = new Scalar(0,0,125);
//        Scalar upper_red = new Scalar(0,0,255);
//        Core.inRange(img, lower_red, upper_red, img);
//
//
//        // Find kanter med canny()
//        int threshold1 = 100;
//        int threshhold2 = 200;
//        //img = detectEdgesCanny(img, threshold1, threshhold2); // denne funk virker kun halvt
//
//        return img;
//    }


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