package dk.localghost.hold17.autonomous_drone.opencv_processing;

import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Rectangle;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.PointComparator;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.core.Core.FILLED;
import static org.opencv.core.Core.FONT_HERSHEY_SCRIPT_SIMPLEX;
import static org.opencv.imgproc.Imgproc.*;

public class RectangleFilter {

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private List<Rectangle> rectangles = new ArrayList<>();
    private List<Rectangle> QRCodes = new ArrayList<>();

    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar RED = new Scalar(0, 0, 255);
    private final Scalar YELLOW = new Scalar(0, 255, 255);
    private final Scalar CYAN = new Scalar(255, 255, 0);

    private Rectangle externalCustomRectangle;
    private Rect biggestQRCode;
    private List<Integer> parents = new ArrayList<>();

    List<Rect> squares = new ArrayList<>();
    List<Rect> idenSquares = new ArrayList<>();

    private int imagenumber = 1;

    public FilterHelper filterHelperter;

    public RectangleFilter() {

        for(int i = 1; i < 27; i ++) {
            System.out.println("Running algorithm on image " + i);
            filterImage(FilterHelper.matToBufferedImage(FilterHelper.openFile(imagenumber + ".jpg")));
            System.out.println("\n\n");
            imagenumber++;
        }

    }




    public static void main(String[] args) {
        new RectangleFilter();
    }

    // TODO: Skift værdierne der tjekkes for, så de passer til dronens kameraopløsning
    public Direction findPaperPosition(Rect rect) {
        if (rect.x > 0 && rect.x < 512) {
            return Direction.LEFT;
        }

        if (rect.x > 512 && rect.x < 768) {
            return Direction.CENTER;
        }

        if (rect.x > 768 && rect.x < 1280) {
            return Direction.RIGHT;
        }
        return Direction.UNKNOWN;
    }

    /***
     * Look through external rectangles and define
     * rectangles with enough children as QRcodes,
     * Find the biggest QRcode by height,
     * Draw it thicker than the other external rectangles
     * @param imgcol Mat
     */
    public void findBiggestQRCode(Mat imgcol) {
        Rect biggestQRCode = null;
        boolean first = true;

        /* Define and find QRCodes as rectangles with 3 or more children */
        for (Rectangle e : rectangles) {
            if (e.getChildren() >= 2) {
                QRCodes.add(e);
                System.out.println("QR code found!");
            }
        }

        /* Determine the largest height */
        for (Rectangle e : QRCodes) {
            if(e.getChildren() >=2) {
                if (first) {
                    biggestQRCode = e.getRect();
                    first = false;
                } else if (e.getRect().height > biggestQRCode.height) {
                    biggestQRCode = e.getRect();
                }
            }
        }

        if (biggestQRCode != null) {
            // set field
            this.biggestQRCode = biggestQRCode;
            Imgproc.rectangle(imgcol, biggestQRCode.br(), biggestQRCode.tl(), NEON_GREEN, 3, 8, 0);
            System.out.println("Biggest QR code is at: " + "(" + biggestQRCode.x + ", " + biggestQRCode.y + ")");

            int x = biggestQRCode.x + biggestQRCode.width/2;
            int y = biggestQRCode.y + biggestQRCode.height/2;
            Point center = new Point(x, y);

            Imgproc.circle(imgcol, center, 15, NEON_GREEN, FILLED);
        }
    }

    /*** Use contours to detect vertices,
     *   Check for all contours with approximately 4 vertices (rectangle),
     *   Surround rectangle with a boundingrect,
     *   Check for boundingrect size,
     *   Draw desired boundingrects
     * @param imgcol Mat
     * @param contours List<MatOfPoint>
     * @param hierarchy Mat (not currently in use, might need)
     * @param accuracy double
     * @return Mat
     */
    public Mat drawRectangles(Mat imgcol, List<MatOfPoint> contours, List<MatOfPoint> externalContours, Mat hierarchy, double accuracy) {
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();

        /* Look through contours */
        for (MatOfPoint contour : contours) {
            matOfPoint2f.fromList(contour.toList());

            /* Check if current contour has approximately approx.total() vertices. We check for 4 = rectangle. */
            Imgproc.approxPolyDP(matOfPoint2f, approx, Imgproc.arcLength(matOfPoint2f, true) * accuracy, true);

            long total = approx.total();

            /* If 4 vertices are found, the contour is approximately a rectangle */
            if (total == 4) {

                /* Create a boundingRect from the current contour. A boundingRect is the smallest
                 * possible rectangle in which the contour will fit */
                Rect rect = Imgproc.boundingRect(contour);

                /* Find the centerpoints and area */
                double centerX = rect.x + (rect.width / 2);
                double centerY = rect.y + (rect.height / 2);
                double rectArea = rect.width * rect.height;
                double squareThreshold = (double) rect.width / (double) rect.height;

                /* If the rect area is over 3000 we want to draw it
                 * Might need to add more requirements */
                if (rectArea > 1000 && rectArea < 50000  && squareThreshold > 0.8 && squareThreshold < 1.2 /*&& aspectRatio > 0.2 /* && rect.width > 50 && rect.height > 100 */) {
                     System.out.print("Rectangle detected. ");
                     System.out.print("Coordinates: " + "(" +centerX + ", " + centerY + ") ");
                     System.out.print("Width: " + rect.width + ", Height: " + rect.height);
                    int index = contours.indexOf(contour);
                    Mat sub;
                    sub = hierarchy.col(index);
                    int[] subArray = new int[(int) sub.total()*sub.channels()];
                    sub.get(0, 0, subArray);
                    int parent = subArray[3];
//                    System.out.println("Hierarchy index " + index + ": " + hierarchy.col(index).dump() + " || PARENT: " + subArray[3]);
                    Rectangle rectangle = new Rectangle(rect);
                    rectangle.setParent(parent);
                    if(!parents.contains(parent)) {
                        parents.add(parent);
                    }
                    System.out.println(" || PARENT: " + rectangle.getParent());
                    rectangles.add(rectangle);

                    String idxstr = "" + index;

//                        double squareThreshold = (double) rect.height / (double) rect.width;
//                        System.out.println("Squarethreshold is: " + squareThreshold);
//                        if (squareThreshold > 0.9 && squareThreshold < 1.1) {
//                            squares.add(rect);
//                        }
//                        System.out.println("Found " + squares.size() + " squares. Checking area...");

//                            Imgproc.rectangle(imgcol, rect.br(), rect.tl(), RED, 3, 8, 0);
                            Point start = new Point(rect.x, rect.y);
                            Imgproc.putText(imgcol, idxstr, start , FONT_HERSHEY_SCRIPT_SIMPLEX, 0.5, CYAN, 1);
                }
            }
        }

        return imgcol;
    }

    public void defineQRCode(Mat imgcol) {

        List<Rectangle> temp = new ArrayList<>();

        for(int i = 0; i < parents.size(); i++) {
            int count = 0;
            temp.clear();
            for(int j = 0; j < rectangles.size(); j++) {
//                System.out.println("Rect parent: " + rectangles.get(j).getParent());
                if(rectangles.get(j).getParent() == parents.get(i)) {
                    temp.add(rectangles.get(j));
                    count++;
                }
            }
            int identsized = 0;

            if(count >= 3) {
                System.out.println("QR code found!");
                for(int k = 0; k < temp.size(); k++) {
                    Rect rect = temp.get(k).getRect();
                    Imgproc.rectangle(imgcol, rect.br(), rect.tl(), RED, 3, 8, 0);
                    }
            }
            else if(count == 2) {
                Rect rect1 = temp.get(0).getRect();
                Rect rect2 = temp.get(1).getRect();
                double thresholdX = (double) rect1.x / (double) rect2.x;
                double thresholdY = (double) rect1.y / (double) rect2.y;
                System.out.println("Threshold x = " + thresholdX);
                System.out.println("Threshold y = " + thresholdY);
                if(thresholdX > 0.60 && thresholdX < 1.40 && thresholdY > 0.60 & thresholdY < 1.40) {
                    System.out.println("QR code found!");
                    Imgproc.rectangle(imgcol, rect1.br(), rect1.tl(), RED, 3, 8, 0);
                    Imgproc.rectangle(imgcol, rect2.br(), rect2.tl(), RED, 3, 8, 0);
                }

            }
        }
    }

    public int averageArea(List<Rect> rects) {
        int avg = 0;
        for(int i = 0; i < rects.size(); i++) {
            avg += rects.get(i).area();
        }
        avg = avg / rects.size();
        return avg;
    }

    public void detectSquares (Mat imgcol) {

            for (int i = 0; i < squares.size(); i++) {
                for (int j = 0; j < squares.size(); j++) {
                    if (i != j) {
                        double areaThreshold = (double) squares.get(i).area() / (double) squares.get(j).area();
                        if (areaThreshold > 0.8 && areaThreshold < 1.2) {
                            System.out.println("Areathreshold is " + areaThreshold);
                            idenSquares.add(squares.get(j));
                        }
                    }
                }
            }

            int avgX = 0;
            int avgY = 0;
            for (int i = 0; i < idenSquares.size(); i++) {
                Rect rect = idenSquares.get(i);
                Imgproc.rectangle(imgcol, rect.br(), rect.tl(), RED, 3, 8, 0);
                avgX += (rect.x + rect.width / 2);
                avgY += (rect.y + rect.height / 2);
            }

            if (!idenSquares.isEmpty() && idenSquares.size() >= 2) {
                avgX = avgX / idenSquares.size();
                avgY = avgY / idenSquares.size();
                Point avg = new Point(avgX, avgY);
                Imgproc.circle(imgcol, avg, 15, NEON_GREEN, FILLED);
            }

        }

    /*** Denoise binary image,
     *   Find contours in binary image,
     *   Convert image to 8-bit then RGB,
     *   Run drawRectangles(),
     *   Run determinePaperOrientation;
     * @return Mat
     */

    public Mat filterImage(BufferedImage bufferedImage) {
        rectangles.clear();
        QRCodes.clear();
        squares.clear();
        idenSquares.clear();
        parents.clear();

        Mat originalImage = null;
        originalImage =  FilterHelper.bufferedImageToMat(bufferedImage);

        Mat imgbin = detectWhiteMat(originalImage);
        Mat imgcol = new Mat();
        Mat hierarchy1 = new Mat();
        Mat hierarchy2 = new Mat();

        //Denoise binary image using medianBlur and OPEN
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        dilate(imgbin, imgbin, kernel);
//        erode(imgbin, imgbin, kernel);
//
//        erode(imgbin, imgbin, kernel);
//        dilate(imgbin, imgbin, kernel);

        //Contours are matrices of points. We store all of them in this list.
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> externalContours = new ArrayList<>();

        /* Convert the binary image to an 8-bit image,
         * then convert to an RGB image so rectangles can be outlined in color */
//        imgbin.convertTo(imgbin, CvType.CV_8UC3);
//        cvtColor(imgbin, imgcol, COLOR_GRAY2RGB);

        /* Find contours in the binary image. RETR_TREE creates a perfect hierarchy of contours,
         * including children, grandchildren, parents and grandparents. Might be useful later, but
         * is not currently used. Use RETR_EXTERNAL if you only want to find parent contours. */
        Mat edges = new Mat();
//        Canny(imgbin, edges, 100, 150, 3, false);
        cvtColor(imgbin, imgcol, COLOR_GRAY2RGB);
        Imgproc.findContours(imgbin, contours, hierarchy1, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//        for(int i = 0; i < hierarchy1.cols(); i++) {
//            System.out.println("Column: " + i + " || " + hierarchy1.col(i).dump());
//
//        }
//        Imgproc.findContours(imgbin, externalContours, hierarchy2, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        Imgproc.drawContours(imgcol, externalContours, -1, CYAN, 1);
        Imgproc.drawContours(imgcol, contours, -1, YELLOW, 1);

        //Detect and draw rectangles on RGB image
        imgcol = drawRectangles(imgcol, contours, externalContours, hierarchy1, 0.06);
//        determinePaperOrientation(imgcol);
        defineQRCode(imgcol);
        FilterHelper.saveFile(imagenumber + "filtered.jpg", imgcol);

        return imgcol;
    }

    public Rect getBiggestQRCode() {
        return biggestQRCode;
    }

    /***
     * Convert image to a binary matrix and remove everything that is not the desired shades of white/grey
     * @return Mat
     */
    public Mat detectWhiteMat(Mat image) {
        Mat imgbin = new Mat();
//        cvtColor(image, imgbin, COLOR_BGR2GRAY);


//        bilateralFilter(image, imgbin, 5, 75, 75);

        /* Write 1 if in range of the two scalars, 0 if not. Binary image result written to imgbin */
//        threshold(imgbin, imgbin, 100, 140, THRESH_OTSU);
        Core.inRange(image, new Scalar(150, 150, 150), new Scalar(255, 255, 255), imgbin);

        return imgbin;
    }

        /***
     * Draw a rotated rectangle rRect around the biggest QRCode,f
     * Find endpoints of rRect,
     * Compare the bottom left and bottom right points:
     * If left is lower, drone is to the left of paper
     * If right is lower, drone is to the right of paper
     * @param imgcol Mat
     */
    public void determinePaperOrientation(Mat imgcol) {
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        MatOfPoint2f boxMop2f = new MatOfPoint2f();
        List<MatOfPoint> rotatedBox = new ArrayList<>();
        RotatedRect rRect = null;

        /* Find the object with the biggest QRCode,
         * then define its rotated rect */
        for (Rectangle e : QRCodes) {
            if (e.getRect() == biggestQRCode) {
                matOfPoint2f.fromList(e.getContour().toList());
                rRect = Imgproc.minAreaRect(matOfPoint2f);
            }
        }

        /* Take the vertices of rRect and store in vertices[], then make a rotatedBox
         * from by drawing the contour. Use approxPolyDP() to approximate the endpoints,
         * and store them in a list. */
         /*** TODO: UNCOMMENT CODE ***/
        Point[] vertices = new Point[4];
        rRect.points(vertices);
        rotatedBox.add(new MatOfPoint(vertices));
        boxMop2f.fromList(rotatedBox.get(0).toList());
        drawContours(imgcol, rotatedBox, 0, YELLOW, 3);

        Imgproc.approxPolyDP(boxMop2f, approx, Imgproc.arcLength(boxMop2f, true) * 0.1, true);
        List<Point> approxList = approx.toList();

        /* Make sure that approxPolyDP found 4 points */
        if (approxList.size() == 4) {

            /* Sort the coordinates in ascending y-coords
             * Index 0 is the lowest point of the rotated rectangle */
            Collections.sort(approxList, PointComparator.Y_COORD);
            Point lower1 = approxList.get(0);
            Point lower2 = approxList.get(1);

          /***  Draw all points as filled circles in red
            for (Point p : approxList) {
                Imgproc.circle(imgcol, new Point(p.x, p.y), 10, RED, Core.FILLED);
            } ***/

            /* If the lowest point is to the right of the second lowest point,
             * the drone is to the right of the paper. Then the opposite.
             */
            if(lower1.x > lower2.x) {
                System.out.println("Lowest point is in the right side of rotated rect at point " + lower1);
                System.out.println("Drone is looking at the QR code from the RIGHT");
                Imgproc.circle(imgcol, lower1, 15, CYAN, FILLED);
            }
            else if(lower1.x < lower2.x) {
                System.out.println("Lowest point is in the right side of rotated rect at point " + lower1);
                System.out.println("Drone is looking at the QR code from the LEFT");
                Imgproc.circle(imgcol, lower1, 15, CYAN, FILLED);
            }
        }
    }


    /***TEST METHOD ## Detect edges using a threshold ***/
//    public Mat detectEdgesThreshold() {
//        Mat imgbin = detectWhiteMat();
//        threshold(imgbin, imgbin, 127, 255, Imgproc.THRESH_BINARY);
//        return imgbin;
//    }

    /***TEST METHOD ## Detect edges using canny ***/
//    public Mat detectEdgesCanny() {
//        Mat imgbin = detectWhiteMat();
//        Imgproc.Canny(imgbin, imgbin, 100, 200);
//        return imgbin;
//    }
}
