package dk.localghost.hold17.autonomous_drone.opencv_processing;

import dk.localghost.hold17.autonomous_drone.controller.QrTracker;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Rectangle;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.*;

public class RectangleFilter implements QrTracker {

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private List<Rectangle> rectangles = new ArrayList<>();

    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar RED = new Scalar(0, 0, 255);
    private final Scalar YELLOW = new Scalar(0, 255, 255);
    private final Scalar CYAN = new Scalar(255, 255, 0);

    //color filter
    private Scalar HSV_FILTER_LOWER = new Scalar(0, 0, 0);
    private Scalar HSV_FILTER_UPPER = new Scalar(179, 255, 254);
    private double lowerThresh = 175;
    private double upperThresh = 200;

    private List<Integer> parents = new ArrayList<>();

    private List<Rect> idenSquares = new ArrayList<>();

    private Direction qrDirection = Direction.UNKNOWN;
    private Direction lastKnownDirection = Direction.UNKNOWN;

    private boolean readyToFlyThroughRing = false;

    public RectangleFilter() {}

//    public RectangleFilter() {
//        filterImage(FilterHelper.openFile("13.jpg"));
//    }


//    public static void main(String[] args) {
//        new RectangleFilter();
//
//    }
    /*** Use contours to detect vertices,
     *   Check for all contours with approximately 4 vertices (rectangle),
     *   Surround rectangle with a boundingrect,
     *   Check for boundingrect size,
     *   Draw desired boundingrects
     * @param imgcol Mat
     * @param contours List<MatOfPoint>
     * @param hierarchy Mat of contour hierarchy levels
     * @return Mat
     */
    private Mat drawRectangles(Mat imgcol, List<MatOfPoint> contours, Mat hierarchy) {
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint2f parentContour = new MatOfPoint2f();
        double accuracy = 0.06;

        /* Look through contours */
        for (MatOfPoint contour : contours) {
            int index = contours.indexOf(contour);
            int level = 0;
            int parent = checkParents(hierarchy, index);

            while (parent != -1) {
                level++;
//                System.out.println("Level: " + level);
                if (level == 4) {
                    parentContour.fromList(contours.get(parent).toList());
                    Imgproc.approxPolyDP(parentContour, approx, Imgproc.arcLength(parentContour, true) * 0.05, true);

                    long total = approx.total();
                    if (total == 4) {
//                        System.out.println("FOUND QR CODE! LEVEL 4 HIERARCHY!");
                        Rect rect = Imgproc.boundingRect(contours.get(parent));
                        Imgproc.rectangle(imgcol, rect.br(), rect.tl(), NEON_GREEN, 2);
                    }
                }

                parent = checkParents(hierarchy, parent);
            }
        }

        return imgcol;
    }

    public int checkParents(Mat hierarchy, int contourIndex) {

        Mat sub = hierarchy.col(contourIndex);
        int[] subArray = new int[(int) sub.total() * sub.channels()];
        sub.get(0, 0, subArray);
        int parent = subArray[3];
//        System.out.println("Hierarchy index " + contourIndex + ": " + hierarchy.col(contourIndex).dump() + " || PARENT: " + subArray[3]);

        if (parent != -1) {
            return parent;
        }

        return -1;
    }



//            matOfPoint2f.fromList(contour.toList());
//
//            /* Check if current contour has approximately approx.total() vertices. We check for 4 = rectangle. */
//            Imgproc.approxPolyDP(matOfPoint2f, approx, Imgproc.arcLength(matOfPoint2f, true) * accuracy, true);
//
//            long total = approx.total();
//
//            /* If 4 vertices are found, the contour is approximately a rectangle */
//            if (total == 4) {
//                /* Create a boundingRect from the current contour. A boundingRect is the smallest
//                 * possible rectangle in which the contour will fit */
//                Rect rect = Imgproc.boundingRect(contour);
//
//                /* Find the centerpoints and area */
//                double centerX = rect.x + (rect.width / 2);
//                double centerY = rect.y + (rect.height / 2);
//                double rectArea = rect.width * rect.height;
//                double squareThreshold = (double) rect.width / (double) rect.height;
//
//                /* If the rect area is over 3000 we want to draw it
//                 * Might need to add more requirements */
//                if (rectArea > 1000 && rectArea < 50000  && squareThreshold > 0.7 && squareThreshold < 1.3) {
////                    System.out.print("Rectangle detected. Coordinates: " + "(" + centerX + ", " + centerY + ") ");
////                    System.out.print("Width: " + rect.width + ", Height: " + rect.height);
//                    int index = contours.indexOf(contour);
//                    Mat sub = hierarchy.col(index);
//                    int[] subArray = new int[(int) sub.total() * sub.channels()];
//                    sub.get(0, 0, subArray);
//                    int parent = subArray[3];
//                    int child = subArray[2];
//                    System.out.println("Hierarchy index " + index + ": " + hierarchy.col(index).dump() + " || PARENT: " + subArray[3]);
//                    Rectangle rectangle = new Rectangle(rect);
//                    rectangle.setParent(parent);
//
//
//
//                    if (rectangle.getParent() != -1) {
//                        if (!parents.contains(parent)) {
//                            parents.add(parent);
//                        }
////                        System.out.print(" || CONTOUR " + index);
////                        System.out.println(" || PARENT: " + rectangle.getParent());
//                       rectangles.add(rectangle);
//
////                         Debug contour index
////                        String idxstr = "" + index;
////                        Point start = new Point(rect.x, rect.y);
////                        Imgproc.putText(imgcol, idxstr, start, Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 0.5, CYAN, 1);
//                    }
//                }
//            }
//        }
//
//        return imgcol;
//    }
//


    private void defineQRCode(Mat imgcol) {
        List<Rectangle> temp = new ArrayList<>();

        for (Integer parent : parents) {
            int count = 0;
            temp.clear();
            for (Rectangle rectangle : rectangles) {
//                System.out.println("Rect parent: " + rectangles.get(j).getParent());
                if (rectangle.getParent() == parent) {
                    temp.add(rectangle);
                    count++;
                }
            }

            if (count >= 3) {
                detectSquares(imgcol, temp);
            } else if (count == 2) {
                Rect rect1 = temp.get(0).getRect();
                Rect rect2 = temp.get(1).getRect();
                double thresholdX = (double) rect1.x / (double) rect2.x;
                double thresholdY = (double) rect1.y / (double) rect2.y;
//                System.out.println("Threshold x = " + thresholdX);
//                System.out.println("Threshold y = " + thresholdY);
                if (thresholdX > 0.60 && thresholdX < 1.40 && thresholdY > 0.60 & thresholdY < 1.40) {
                    detectSquares(imgcol, temp);
                }

            }
        }
    }

    public void checkHierarchy(List<MatOfPoint> contours, Mat hi) {
    }

    public void detectSquares (Mat imgcol, List<Rectangle> rectangles) {
        idenSquares.clear();
        for (int i = 0; i < rectangles.size(); i++) {
            for (int j = 0; j < rectangles.size(); j++) {
                if (i != j) {
                    double areaThreshold = rectangles.get(i).getRect().area() / rectangles.get(j).getRect().area();

                    if (areaThreshold > 0.8 && areaThreshold < 1.2) {
                        idenSquares.add(rectangles.get(j).getRect());
                    }
                }
            }
        }

        int avgX = 0;
        int avgY = 0;
        int avgArea = 0;
        for (Rect rect : idenSquares) {
            Imgproc.rectangle(imgcol, rect.br(), rect.tl(), RED, 3, 8, 0);
            avgX += (rect.x + rect.width / 2);
            avgY += (rect.y + rect.height / 2);
            avgArea += rect.area();
        }

        if (!idenSquares.isEmpty() && idenSquares.size() >= 2) {
            avgX = avgX / idenSquares.size();
            avgY = avgY / idenSquares.size();
            avgArea = avgArea / idenSquares.size();
            Point avg = new Point(avgX, avgY);
            Imgproc.circle(imgcol, avg, 15, NEON_GREEN, Core.FILLED);
            if(avgArea > 10000) {
                readyToFlyThroughRing = true;
            }

        }

        qrDirection = Direction.findXDirection(avgX);

        if (qrDirection != Direction.UNKNOWN) {
            lastKnownDirection = qrDirection;
        }

    }

    /*** Denoise binary image,
     *   Find contours in binary image,
     *   Convert image to 8-bit then RGB,
     *   Run drawRectangles(),
     *   Run determinePaperOrientation;
     * @return Mat
     */

    public Mat filterImage(Mat originalImage) {
        rectangles.clear();
        idenSquares.clear();
        parents.clear();
        readyToFlyThroughRing = false;

        Mat imgbin = detectWhiteMat(originalImage);
        Mat imgcol = new Mat();
        Mat hierarchy = new Mat();

        //Widen contrasts (hence contours) with dilate
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        dilate(imgbin, imgbin, kernel);

        //Contours are matrices of points. We store all of them in this list.
        List<MatOfPoint> contours = new ArrayList<>();

        /* Find contours in the binary image. RETR_TREE creates a perfect hierarchy of contours,
         * including children, grandchildren, parents and grandparents. Might be useful later, but
         * is not currently used. Use RETR_EXTERNAL if you only want to find parent contours. */
        cvtColor(imgbin, imgcol, COLOR_GRAY2RGB);
        Imgproc.findContours(imgbin, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        drawContours(imgcol, contours, -1, YELLOW, 2);
        //Detect and draw rectangles on RGB image
        imgcol = drawRectangles(imgcol, contours, hierarchy);
        defineQRCode(imgcol);

//        FilterHelper.saveFile("13filtered.jpg", imgcol);
        return imgcol;
    }

    /***
     * Convert image to a binary matrix and remove everything that is not the desired shades of white/grey
     * @return Mat
     */
    private Mat detectWhiteMat(Mat image) {
        Mat imgbin = new Mat();
        Mat imghsv = new Mat();
        Mat imgfinal = new Mat();

        GaussianBlur(image, image, new Size(3, 3), 2.0, 2.0);

        cvtColor(image, imgbin, COLOR_BGR2GRAY);

//        Core.bitwise_and(imgbin, imgbin, imgfinal, imghsv);
//        equalizeHist(imgfinal, imgfinal);

        threshold(imgbin, imgbin, lowerThresh, upperThresh, THRESH_BINARY_INV);
//        Core.inRange(image, new Scalar(150, 150, 150), new Scalar(255, 255, 255), imgbin);

        // convert to HSV
//        cvtColor(image, imghsv, Imgproc.COLOR_BGR2HSV);
//        List<Mat> channels = new ArrayList<>();
//        Core.split(imghsv, channels);
//        equalizeHist(channels.get(1), channels.get(1));
//        equalizeHist(channels.get(2), channels.get(2));
//        Core.merge(channels, imghsv);

        // filter lower and upper red
//        Core.inRange(imghsv, HSV_FILTER_LOWER, HSV_FILTER_UPPER, imghsv);

        return imgbin;
    }


    @Override
    public Direction getFlightDirection() {
        return qrDirection;
    }

    @Override
    public Direction getLastKnownDirection() {
        return lastKnownDirection;
    }

    @Override
    public void resetFlightDirection() {
        qrDirection = Direction.UNKNOWN;
    }

    @Override
    public boolean readyForFlyingThroughRing() {
        return readyToFlyThroughRing;
    }

//    public int averageArea(List<Rect> rects) {
//        int avg = 0;
//        for(int i = 0; i < rects.size(); i++) {
//            avg += rects.get(i).area();
//        }
//        avg = avg / rects.size();
//        return avg;
//    }

//        /***
//     * Draw a rotated rectangle rRect around the biggest QRCode,f
//     * Find endpoints of rRect,
//     * Compare the bottom left and bottom right points:
//     * If left is lower, drone is to the left of paper
//     * If right is lower, drone is to the right of paper
//     * @param imgcol Mat
//     */
//     public void determinePaperOrientation(Mat imgcol) {
//        MatOfPoint2f approx = new MatOfPoint2f();
//        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
//        MatOfPoint2f boxMop2f = new MatOfPoint2f();
//        List<MatOfPoint> rotatedBox = new ArrayList<>();
//        RotatedRect rRect = null;
//
//        /* Find the object with the biggest QRCode,
//         * then define its rotated rect */
//        for (Rectangle e : QRCodes) {
//            if (e.getRect() == biggestQRCode) {
//                matOfPoint2f.fromList(e.getContour().toList());
//                rRect = Imgproc.minAreaRect(matOfPoint2f);
//            }
//        }
//
//        /* Take the vertices of rRect and store in vertices[], then make a rotatedBox
//         * from by drawing the contour. Use approxPolyDP() to approximate the endpoints,
//         * and store them in a list. */
//         /*** TODO: UNCOMMENT CODE ***/
//        Point[] vertices = new Point[4];
//        rRect.points(vertices);
//        rotatedBox.add(new MatOfPoint(vertices));
//        boxMop2f.fromList(rotatedBox.get(0).toList());
//        drawContours(imgcol, rotatedBox, 0, YELLOW, 3);
//
//        Imgproc.approxPolyDP(boxMop2f, approx, Imgproc.arcLength(boxMop2f, true) * 0.1, true);
//        List<Point> approxList = approx.toList();
//
//        /* Make sure that approxPolyDP found 4 points */
//        if (approxList.size() == 4) {
//
//            /* Sort the coordinates in ascending y-coords
//             * Index 0 is the lowest point of the rotated rectangle */
//            Collections.sort(approxList, PointComparator.Y_COORD);
//            Point lower1 = approxList.get(0);
//            Point lower2 = approxList.get(1);
//
//          /***  Draw all points as filled circles in red
//            for (Point p : approxList) {
//                Imgproc.circle(imgcol, new Point(p.x, p.y), 10, RED, Core.FILLED);
//            } ***/
//
//            /* If the lowest point is to the right of the second lowest point,
//             * the drone is to the right of the paper. Then the opposite.
//             */
//            if(lower1.x > lower2.x) {
//                System.out.println("Lowest point is in the right side of rotated rect at point " + lower1);
//                System.out.println("Drone is looking at the QR code from the RIGHT");
//                Imgproc.circle(imgcol, lower1, 15, CYAN, Core.FILLED);
//            }
//            else if(lower1.x < lower2.x) {
//                System.out.println("Lowest point is in the right side of rotated rect at point " + lower1);
//                System.out.println("Drone is looking at the QR code from the LEFT");
//                Imgproc.circle(imgcol, lower1, 15, CYAN, Core.FILLED);
//            }
//        }
//    }

    public int getFilter1LowerHue() {
        return (int) HSV_FILTER_LOWER.val[0];
    }

    public void setFilter1LowerHue(double h1) {
        HSV_FILTER_LOWER.set(new double[]{h1, HSV_FILTER_LOWER.val[1], HSV_FILTER_LOWER.val[2]});
    }

    public int getFilter1LowerSat() {
        return (int) HSV_FILTER_LOWER.val[1];
    }

    public void setFilter1LowerSat(double s1) {
        HSV_FILTER_LOWER.set(new double[]{HSV_FILTER_LOWER.val[0], s1, HSV_FILTER_LOWER.val[2]});
    }

    public int getFilter1LowerVal() {
        return (int) HSV_FILTER_LOWER.val[2];
    }

    public void setFilter1LowerVal(double v1) {
        HSV_FILTER_LOWER.set(new double[]{HSV_FILTER_LOWER.val[0], HSV_FILTER_LOWER.val[1], v1});
    }

    public int getFilter1UpperHue() {
        return (int) HSV_FILTER_UPPER.val[0];
    }

    public void setFilter1UpperHue(double h2) {
        HSV_FILTER_UPPER.set(new double[]{h2, HSV_FILTER_UPPER.val[1], HSV_FILTER_UPPER.val[2]});
    }

    public int getFilter1UpperSat() {
        return (int) HSV_FILTER_UPPER.val[1];
    }

    public void setFilter1UpperSat(double s2) {
        HSV_FILTER_UPPER.set(new double[]{HSV_FILTER_UPPER.val[0], s2, HSV_FILTER_UPPER.val[2]});
    }

    public int getFilter1UpperVal() {
        return (int) HSV_FILTER_UPPER.val[2];
    }

    public void setFilter1UpperVal(double v2) {
        HSV_FILTER_UPPER.set(new double[]{HSV_FILTER_UPPER.val[0], HSV_FILTER_UPPER.val[1], v2});
    }

    public double getLowerThresh() {
        return lowerThresh;
    }

    public void setLowerThresh(double lowerThresh) {
        this.lowerThresh = lowerThresh;
    }

    public double getUpperThresh() {
        return upperThresh;
    }

    public void setUpperThresh(double upperThresh) {
        this.upperThresh = upperThresh;
    }
}
