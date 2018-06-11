package dk.localghost.hold17.autonomous_drone.opencv_processing.filter;

import dk.localghost.hold17.autonomous_drone.controller.DroneController;
import dk.localghost.hold17.autonomous_drone.opencv_processing.Direction;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.cvtColor;

// TODO: Clean up unused code
public class CircleFilter {

    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar RED = new Scalar(0, 0, 255);
    private final Scalar YELLOW = new Scalar(0, 255, 255);
    private final Scalar CYAN = new Scalar(255, 255, 0);

    private String fileName = "4.jpg";
    private String outputName = "4filtered.jpg";
    private String imgNumber = "4";

    private DroneController droneController;

    public Point getBiggestCircle() {
        return biggestCircle;
    }

    // Global variabel for centrum af største cirkel.
    private Point biggestCircle = new Point();

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    //Udkommenteret test funktion
    public CircleFilter(final DroneController droneController) {
        this.droneController = droneController;
//        BufferedImage img = matToBufferedImage(openFile(fileName));
//        benchmark(Shape.CIRCLE);
//        benchmark(Shape.RECTANGLE);

//        Mat img_circle = openFile("3.jpg");
//        findCircleAndDraw(img_circle, 1, 150);
//        Direction direction = findDirectionFromCircle(biggestCircle);
//        System.out.println(direction);
//        saveFile(outputName, img_circle);
    }

    //    public static void main(String[] args) {
//        new CircleFilter();
//    }
    /*
     * Funktion der finder cirkler samt tegner dem
     * dp = pixelreduktionsgrad
     * minDist = minimum radius i pixelantal
     * TODO: Metode kan forveksle den største cirkel i scenarier hvor der er flere cirkler.
     */
    public Mat findCircleAndDraw(Mat image, int dp, int minDist) {
        Mat circlePosition = new Mat();
        Mat hsv_image = new Mat();
        //Parameter tjek
        if (dp <= 0) dp = 1;
        if (minDist <= 0) minDist = 1;

        cvtColor(image, hsv_image, Imgproc.COLOR_BGR2HSV);
        Mat lower_red = new Mat();
        Mat upper_red = new Mat();

        Core.inRange(hsv_image, new Scalar(0, 100, 100), new Scalar(10, 255, 255), lower_red);
        Core.inRange(hsv_image, new Scalar(160, 100, 100), new Scalar(179, 255, 255), upper_red);

        Mat red_hue_image = new Mat();
        Core.addWeighted(lower_red, 1, upper_red, 1, 0, red_hue_image);

        GaussianBlur(red_hue_image, red_hue_image, new Size(9, 9), 2, 2);

        Imgproc.HoughCircles(red_hue_image, circlePosition, Imgproc.CV_HOUGH_GRADIENT, dp, red_hue_image.rows() / 8, 100, 30, 50, 10000);

        // finder objekter der ligner cirkler og gemmer deres position i circlePosition

        // fortsæt kun, hvis der er fundet én eller flere cirkler
        Point maxCenter;
//        if (circlePosition.empty() == false) {
            //System.out.println("Fandt: " + circlePosition.cols() + " cirkler");

            // sætter cirklens farve
            double farve = 100;
            Scalar color = new Scalar(farve);

            int maxRadius = 0;
            for (int i = 0; i < circlePosition.cols(); i++) // antallet af kolonner angiver antallet af cirkler fundet
            {
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
            Imgproc.circle(image, biggestCircle, maxRadius, color);
//        } else {
//            System.out.println("Der blev ikke fundet nogle cirkler i billedet");
//            return null;
//        }
        return image;
    }

    public Direction findDirectionFromCircle(Point circleCoordinate) {

        if (circleCoordinate == null) {
            System.out.println("Point er ikke initialiseret");
            return Direction.UNKNOWN;
        } else {
            double x = circleCoordinate.x;
            if (x > 0 && x < 512) return Direction.LEFT;
            else if (x > 512 && x < 768) return Direction.CENTER; // 256px (1/5 af billedeopløsningen på 1280)
            else if (x > 768 && x < 1280) return Direction.RIGHT;
            else {
                System.err.println("Cannot find the direction to the circle is the resolution correct?");
                System.err.println("Current resolution is ");
                return Direction.UNKNOWN;
            }
        }
    }

    public Direction findDirectionFromCircleGrid(Point circleCoordinate) {
        if (circleCoordinate == null) {
            System.out.println("Point er ikke initialiseret");
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
}