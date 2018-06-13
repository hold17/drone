package dk.localghost.hold17.autonomous_drone.opencv_processing;

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

//    private final Scalar RED = new Scalar(0, 0, 255);
//    private final Scalar YELLOW = new Scalar(0, 255, 255);
//    private final Scalar CYAN = new Scalar(255, 255, 0);
//    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar HSV_NEON_GREEN = new Scalar(86, 240, 129);
    private final Scalar HSV_DEEP_BLUE = new Scalar(120, 90, 70);

    // red-green
    private Scalar HSV_FILTER1_LOWER = new Scalar(0, 77, 55);
    private Scalar HSV_FILTER1_UPPER = new Scalar(5, 221, 255);
    // blue-red
    private Scalar HSV_FILTER2_LOWER =  new Scalar(141, 74, 55);
    private Scalar HSV_FILTER2_UPPER = new Scalar(179, 223, 191);

    private int param1 = 200;
    private int param2 = 30;

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

    public CircleFilter() {}

    /*
     * Funktion der finder cirkler samt tegner dem
     * TODO: Metode kan forveksle den største cirkel i scenarier hvor der er flere cirkler.
     */
    public Mat findCircleAndDraw(Mat image) {
        biggestCircle = new Point();
        Mat circlePosition = new Mat();
        Mat hsv_image = new Mat();
        Mat lower_red = new Mat();
        Mat upper_red = new Mat();
        Mat red_hue_image = new Mat();
        Mat filteredOverlay = new Mat();

        // convert to HSV
        cvtColor(image, hsv_image, Imgproc.COLOR_BGR2HSV);

        // filter lower and upper red
        Core.inRange(hsv_image, HSV_FILTER1_LOWER, HSV_FILTER1_UPPER, lower_red);
        Core.inRange(hsv_image, HSV_FILTER2_LOWER, HSV_FILTER2_UPPER, upper_red);

        // combine filtered images and blur the result
        Core.addWeighted(lower_red, 1, upper_red, 1, 0, red_hue_image);
        GaussianBlur(red_hue_image, red_hue_image, new Size(9, 9), 2, 2);

        // finder objekter der ligner cirkler og gemmer deres position i circlePosition
        // 4th argument = downscaling. Higher values = lower resolution. 1 = no downscaling.
        // 5th argument = minDist between circles
        Imgproc.HoughCircles(red_hue_image, circlePosition, Imgproc.CV_HOUGH_GRADIENT, 1, (red_hue_image.rows()/8), param1, param2, 100, 640);

        Core.bitwise_and(image, image, filteredOverlay, red_hue_image);

        // fortsæt kun, hvis der er fundet én eller flere cirkler
        if (!circlePosition.empty()) {
            int maxRadius = 0;
            double[] testArr;
            Point center;
            int radius;
            // antallet af kolonner angiver antallet af cirkler fundet
            for (int i = 0; i < circlePosition.cols(); i++) {
                testArr = circlePosition.get(0, i);
                //System.out.println("\nCirkel nr. " + (i + 1) + " fundet på:\nx-koord: " + testArr[0] + "\ny-koord: " + testArr[1] + "\nradius: " + testArr[2]);

                // sætter cirklens centrum
                center = new Point(testArr[0], testArr[1]);

                // parser radius til int for at efterleve parametre krav i circle()
                radius = (int) testArr[2];

                // Vi ønsker kun at tegne den største cirkel
                if (maxRadius < radius) {
                    maxRadius = radius;
                    biggestCircle.x = center.x;
                    biggestCircle.y = center.y;
                }
            }
            // debug
//            System.out.println("Koordinater for fundne cirkel" + biggestCircle);

            averageCenterArray.add(biggestCircle);
//            System.out.println("RADIUS: " + maxRadius);

            // tegn den største fundne cirkel på billedet
            Imgproc.circle(filteredOverlay, biggestCircle, maxRadius, HSV_NEON_GREEN, 3, 8, 0);
        }
        // det gennemsnitlige center udregnes
        Point tempAverage = calculateAverageCenter(averageCenterArray);
//        System.out.println("AVERAGE: " + tempAverage);

        // tegner gennemsnit
        Imgproc.circle(filteredOverlay, tempAverage, 10, HSV_DEEP_BLUE, 8);
        return filteredOverlay;
    }

    private Point calculateAverageCenter(List<Point> points){
        double tempx = 0;
        double tempy = 0;

        // TODO
        if (points.size() >= 29) points.clear();


        if (!points.isEmpty())
        {
            for (Point p : points)
            {
                if (points.size() >= 30){ // maks 30 elementer i listen
//                    System.out.println("arraylist full");
                } else {
                    tempx += p.x;
                    tempy += p.y;
                }
            }
        } else{
//            System.out.println("Tomt array i calculateAverageCenter()");
        }

        int arraySize = points.size();
        averageCenter.x = tempx / arraySize;
        averageCenter.y = tempy / arraySize;
        return averageCenter;
    }


    public void clearAverageArray(){
        averageCenterArray.clear();
    }

    public int getFilter1LowerHue() {
        return (int) HSV_FILTER1_LOWER.val[0];
    }

    public void setFilter1LowerHue(double h1) {
        HSV_FILTER1_LOWER.set(new double[]{h1, HSV_FILTER1_LOWER.val[1], HSV_FILTER1_LOWER.val[2]});
    }

    public int getFilter1LowerSat() {
        return (int) HSV_FILTER1_LOWER.val[1];
    }

    public void setFilter1LowerSat(double s1) {
        HSV_FILTER1_LOWER.set(new double[]{HSV_FILTER1_LOWER.val[0], s1, HSV_FILTER1_LOWER.val[2]});
    }

    public int getFilter1LowerVal() {
        return (int) HSV_FILTER1_LOWER.val[2];
    }

    public void setFilter1LowerVal(double v1) {
        HSV_FILTER1_LOWER.set(new double[]{HSV_FILTER1_LOWER.val[0], HSV_FILTER1_LOWER.val[1], v1});
    }

    public int getFilter1UpperHue() {
        return (int) HSV_FILTER1_UPPER.val[0];
    }

    public void setFilter1UpperHue(double h2) {
        HSV_FILTER1_UPPER.set(new double[]{h2, HSV_FILTER1_UPPER.val[1], HSV_FILTER1_UPPER.val[2]});
    }

    public int getFilter1UpperSat() {
        return (int) HSV_FILTER1_UPPER.val[1];
    }

    public void setFilter1UpperSat(double s2) {
        HSV_FILTER1_UPPER.set(new double[]{HSV_FILTER1_UPPER.val[0], s2, HSV_FILTER1_UPPER.val[2]});
    }

    public int getFilter1UpperVal() {
        return (int) HSV_FILTER1_UPPER.val[2];
    }

    public void setFilter1UpperVal(double v2) {
        HSV_FILTER1_UPPER.set(new double[]{HSV_FILTER1_UPPER.val[0], HSV_FILTER1_UPPER.val[1], v2});
    }

    public int getFilter2LowerHue() {
        return (int) HSV_FILTER2_LOWER.val[0];
    }

    public void setFilter2LowerHue(double h3) {
        HSV_FILTER2_LOWER.set(new double[]{h3, HSV_FILTER2_LOWER.val[1], HSV_FILTER2_LOWER.val[2]});
    }

    public int getFilter2LowerSat() {
        return (int) HSV_FILTER2_LOWER.val[1];
    }

    public void setFilter2LowerSat(double s3) {
        HSV_FILTER2_LOWER.set(new double[]{HSV_FILTER2_LOWER.val[0], s3, HSV_FILTER2_LOWER.val[2]});
    }

    public int getFilter2LowerVal() {
        return (int) HSV_FILTER2_LOWER.val[2];
    }

    public void setFilter2LowerVal(double v3) {
        HSV_FILTER2_LOWER.set(new double[]{HSV_FILTER2_LOWER.val[0], HSV_FILTER2_LOWER.val[1], v3});
    }

    public int getFilter2UpperHue() {
        return (int) HSV_FILTER2_UPPER.val[0];
    }

    public void setFilter2UpperHue(double h4) {
        HSV_FILTER2_UPPER.set(new double[]{h4, HSV_FILTER2_UPPER.val[1], HSV_FILTER2_UPPER.val[2]});
    }

    public int getFilter2UpperSat() {
        return (int) HSV_FILTER2_UPPER.val[1];
    }

    public void setFilter2UpperSat(double s4) {
        HSV_FILTER2_UPPER.set(new double[]{HSV_FILTER2_UPPER.val[0], s4, HSV_FILTER2_UPPER.val[2]});
    }

    public int getFilter2UpperVal() {
        return (int) HSV_FILTER2_UPPER.val[2];
    }

    public void setFilter2UpperVal(double v4) {
        HSV_FILTER2_UPPER.set(new double[]{HSV_FILTER2_UPPER.val[0], HSV_FILTER2_UPPER.val[1], v4});
    }

    public int getParam1() {
        return this.param1;
    }

    public void setParam1(int param1) {
        this.param1 = param1;
    }

    public int getParam2() {
        return this.param2;
    }

    public void setParam2(int param2) {
        this.param2 = param2;
    }
}
