package dk.localghost.hold17.autonomous_drone.opencv_processing;

import com.sun.org.apache.bcel.internal.generic.SWITCH;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.cvtColor;

// TODO: Clean up unused code
public class CircleFilter {

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    Direction direction;
    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar RED = new Scalar(0, 0, 255);
    private final Scalar YELLOW = new Scalar(0, 255, 255);
    private final Scalar CYAN = new Scalar(255, 255, 0);

//    private String fileName = "4.jpg";
//    private String outputName = "4filtered.jpg";
//    private String imgNumber = "4";

//    private DroneController droneController;

    public Point getBiggestCircle() {
        return biggestCircle;
    }

    public Point getAverageCenter(){
        return averageCenter;
    }

    // Global variabel for centrum af største cirkel.
    private Point biggestCircle = new Point();
    private Point averageCenter = new Point();
    private List<Point> averageCenterArray = new ArrayList<>();

    public CircleFilter() {
////        BufferedImage img = matToBufferedImage(openFile(fileName));
//        filterHelper = new FilterHelper();
//        BufferedImage img_circle = filterHelper.matToBufferedImage(filterHelper.openFile("3.jpg"));
////        Direction direction = findDirectionFromCircle(biggestCircle);
////        System.out.println(direction);
//        filterHelper.saveFile(outputName, findCircleAndDraw(img_circle));
    }

    public static void main(String[] args) {
        new CircleFilter();
    }

    /*
     * Funktion der finder cirkler samt tegner dem
     * TODO: Metode kan forveksle den største cirkel i scenarier hvor der er flere cirkler.
     */
    public Mat findCircleAndDraw(Mat image) {
        biggestCircle = new Point();

        System.out.println(image);
        Mat circlePosition = new Mat();
        Mat hsv_image = new Mat();

//        medianBlur(image,image,3);
        cvtColor(image, hsv_image, Imgproc.COLOR_BGR2HSV);
        Mat lower_red = new Mat();
        Mat upper_red = new Mat();

        Core.inRange(hsv_image, new Scalar(0, 100, 100), new Scalar(10, 255, 255), lower_red);
        Core.inRange(hsv_image, new Scalar(160, 100, 100), new Scalar(179, 255, 255), upper_red);

        Mat red_hue_image = new Mat();
        Core.addWeighted(lower_red, 1, upper_red, 1, 0, red_hue_image);

        GaussianBlur(red_hue_image, red_hue_image, new Size(9, 9), 2, 2);

        // 4th argument = downscaling. Higher values = lower resolution. 1 = no downscaling.
        // 5th argument = minDist between circles
        Imgproc.HoughCircles(red_hue_image, circlePosition, Imgproc.CV_HOUGH_GRADIENT, 1, (red_hue_image.rows() / 8), 110, 18, 50, 400);


        // finder objekter der ligner cirkler og gemmer deres position i circlePosition
        // fortsæt kun, hvis der er fundet én eller flere cirkler
        if (circlePosition.empty() == false) {
//        Point maxCenter;
            //System.out.println("Fandt: " + circlePosition.cols() + " cirkler");

            // sætter cirklens farve
            Scalar color = new Scalar(100);

            int maxRadius = 0;
            for (int i = 0; i < circlePosition.cols(); i++) // antallet af kolonner angiver antallet af cirkler fundet
            {
                double[] testArr = circlePosition.get(0, i);
//                //System.out.println("\nCirkel nr. " + (i + 1) + " fundet på:\nx-koord: " + testArr[0] + "\ny-koord: " + testArr[1] + "\nradius: " + testArr[2]);
                // sætter cirklens centrum
                Point center = new Point(testArr[0], testArr[1]);
//
//                // parser radius til int for at efterleve parametre krav i circle()
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
//            cvtColor(upper_red, red_hue_image, COLOR_GRAY2BGR);

            // debug
            System.out.println("Koordinater for fundne cirkel" + biggestCircle);


            Imgproc.circle(image, biggestCircle, maxRadius, NEON_GREEN, 8);

            averageCenterArray.add(biggestCircle);

            System.out.println("RADIUS: " + maxRadius);
        } else {
            System.out.println("Fandt ingen cirkler...");
            return image;
        }
        Point tempAverage = calculateAverageCenter(averageCenterArray);
        System.out.println("AVERAGE: " + tempAverage);

        // tegner gennemsnit
        Imgproc.circle(image, tempAverage, 10, CYAN, 8);

        return image;
    }

    private Point calculateAverageCenter(List<Point> arr){
        double tempx = 0;
        double tempy = 0;

        // TODO
        if (arr.size() >= 29) arr.clear();


        if (arr.isEmpty() == false)
        {
            for (Point p : arr)
            {
                if (arr.size() >= 30){ // maks 30 elementer i listen
                    System.out.println("araylist full");
                } else {
                    tempx += p.x;
                    tempy += p.y;
                }
            }
        } else{
            System.out.println("Tomt array i calculateAverageCenter()");
        }

        int arraySize = arr.size();
        averageCenter.x = tempx / arraySize;
        averageCenter.y = tempy / arraySize;
        return averageCenter;
}



//    public Direction findDirectionFromCircle(Point circleCoordinate) {
//        if (circleCoordinate == null) {
//            System.out.println("Point er ikke initialiseret");
//            return Direction.UNKNOWN;
//        } else {
//            double x = circleCoordinate.x;
////            double y = circleCoordinate.y;
//            return Direction.findXDirection(x);
//        }
//    }

    //    public Direction findDirectionFromCircle(Point circleCoordinate) {
//
//        if (circleCoordinate == null) {
//            System.out.println("Point er ikke initialiseret");
//            return Direction.UNKNOWN;
//        } else {
//            double x = circleCoordinate.x;
//            if (x > 0 && x < 512) return Direction.LEFT;
//            else if (x > 512 && x < 768) return Direction.CENTER; // 256px (1/5 af billedeopløsningen på 1280)
//            else if (x > 768 && x < 1280) return Direction.RIGHT;
//            else {
//                System.err.println("Cannot find the direction to the circle is the resolution correct?");
//                System.err.println("Current resolution is ");
//                return Direction.UNKNOWN;
//            }
//        }
//    }


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