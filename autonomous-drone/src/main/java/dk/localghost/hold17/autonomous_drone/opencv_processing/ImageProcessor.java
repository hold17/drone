package dk.localghost.hold17.autonomous_drone.opencv_processing;

import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.*;

public class ImageProcessor {

    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar RED = new Scalar (0, 0, 255);
    private final Scalar YELLOW = new Scalar(0, 255, 255);
    private final Scalar CYAN = new Scalar(255, 255, 0);

    private List<Rect> externalRects = new ArrayList<>();
    private List<ExternalRectangle> externalCustomRectangles = new ArrayList<>();
    private List<ExternalRectangle> QRCodes = new ArrayList<>();

    private ExternalRectangle externalCustomRectangle;
    private Rect biggestQRCode;

    private String fileName = "4.jpg";
    private String outputName = "4filtered.jpg";
    private String imgNumber = "4";

    private int count;

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public ImageProcessor() {
//        try {
//            BufferedImage img = matToBufferedImage(openFile(fileName));
//            /* filterImage() runs detectWhiteMat(), then finds contours and runs drawRectangles() */
//            int counter = 0;
//            long startTime = System.currentTimeMillis();
//            int count = 0;
//            while(10000 >  System.currentTimeMillis() - startTime) {
//                externalCustomRectangles.clear();
//                externalRects.clear();
//                QRCodes.clear();
//                filterImage(img);
//                counter++;
//            }
////            saveFile(outputName, filterImage());
//            long stopTime = System.currentTimeMillis();
//            System.out.println("Total time: " + (double)(stopTime-startTime)/1000 + " seconds.");
//            System.out.println("Program ran " + counter + " times.");
//            System.out.println("Ran rectangle() " + count + " times.");
//        } catch (Exception e) {
//            System.err.println("Something went wrong: " + e.toString());
//            e.printStackTrace();
//        }
    }

//    public static void main(String[] args) {
//        new ImageProcessor();
//    }

    /***
     * Open file as matrix
     * @param fileName file
     * @return Mat
     */
    public Mat openFile(String fileName) {
        try {
            final String path = Paths.get("").toAbsolutePath().toString();
            final String filePath = (path + "/DroneImages/" + fileName).replace('/', '\\');

            Mat newImage = Imgcodecs.imread(filePath);
            if (newImage.dataAddr() == 0) {
                throw new Exception("Couldn't open file " + filePath);
            }

            return newImage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*** Save matrix to file ***/
    public void saveFile(String fileName, Mat testMat) {
        final String path = Paths.get("").toAbsolutePath().toString();
            final String filePath = (path + "/DroneImagesFiltered/" + fileName).replace('/', '\\');
        Imgcodecs.imwrite(filePath, testMat);
        System.out.println("File saved to " + filePath);
    }

    /***
     * Convert image to a binary matrix and remove everything that is not the desired shades of white/grey
     * @return Mat
     */
    public Mat detectWhiteMat(Mat image) {
        Mat imgbin = new Mat();

        /* Write 1 if in range of the two scalars, 0 if not. Binary image result written to imgbin */
        Core.inRange(image, new Scalar(200, 200, 200), new Scalar(100, 100, 100), imgbin);

        return imgbin;
    }

    /*** Denoise binary image,
     *   Find contours in binary image,
     *   Convert image to 8-bit then RGB,
     *   Run drawRectangles(),
     *   Run determinePaperOrientation;
     * @return Mat
     */
    public Mat filterImage(BufferedImage bufferedImage) {
        externalCustomRectangles.clear();
        externalRects.clear();
        QRCodes.clear();

        Mat originalImage = null;

        try {
            originalImage = bufferedImageToMat(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //saveFile("originialImage.jpg", originalImage);

        Mat imgbin = detectWhiteMat(originalImage);
        Mat imgcol = new Mat();
        Mat hierarchy1 = new Mat();
        Mat hierarchy2 = new Mat();

        //Denoise binary image using medianBlur and OPEN
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
//        Imgproc.medianBlur(imgbin, imgbin, 5);
//        Imgproc.morphologyEx(imgbin, imgbin, Imgproc.MORPH_OPEN, kernel);

        //Contours are matrices of points. We store all of them in this list.
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> externalContours = new ArrayList<>();

        /* Convert the binary image to an 8-bit image,
         * then convert to an RGB image so rectangles can be outlined in color */
        imgbin.convertTo(imgbin, CvType.CV_8UC3);
        cvtColor(imgbin, imgcol, COLOR_GRAY2RGB);

        /* Find contours in the binary image. RETR_TREE creates a perfect hierarchy of contours,
         * including children, grandchildren, parents and grandparents. Might be useful later, but
         * is not currently used. Use RETR_EXTERNAL if you only want to find parent contours. */
        Imgproc.findContours(imgbin, contours, hierarchy1, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(imgbin, externalContours, hierarchy2, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        Imgproc.drawContours(imgcol, externalContours, -1, CYAN, 3);

        //Detect and draw rectangles on RGB image
        imgcol = drawRectangles(imgcol,contours, externalContours, hierarchy1,0.06);
//        determinePaperOrientation(imgcol);
//        saveFile("filtered.jpg", imgcol);

        return imgcol;
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

        for(MatOfPoint externalContour : externalContours) {
            matOfPoint2f.fromList(externalContour.toList());

            /* Check if current contour has approximately approx.total() vertices. We check for 4 = rectangle. */
            Imgproc.approxPolyDP(matOfPoint2f, approx,Imgproc.arcLength(matOfPoint2f, true) * accuracy, true);

            long total = approx.total();

            /* If 4 vertices are found, the contour is approximately a rectangle */
            if(total == 4) {
                Rect rect = Imgproc.boundingRect(externalContour);

                /* Find the centerpoints and area */
                double centerX = rect.x + (rect.width / 2);
                double centerY = rect.y + (rect.height / 2);
                double rectArea = rect.width * rect.height;

                if (rectArea > 2000 /* && aspectRatio > 0.2 /* && rect.width > 50 && rect.height > 100 */) {
                 /***   System.out.print("External rectangle detected. ");
                    System.out.print("Coordinates: " + "(" + centerX + ", " + centerY + ") ");
                    System.out.println("Width: " + rect.width + ", Height: " + rect.height); ***/
//                    Imgproc.rectangle(imgcol, rect.br(), rect.tl(), NEON_GREEN, 4, 8, 0);
//                    saveFile(imgNumber + "ExternalRect.jpg", imgcol);
                    //Create rotated rectangle by defining the minimum area in which the contour will fit
//                    MatOfPoint2f rectContour = null;
//                    rectContour.fromList(externalContour.toList());
//                    RotatedRect rRect = Imgproc.minAreaRect(approx);
                    externalRects.add(rect);
                    externalCustomRectangle = new ExternalRectangle(rect);
                    externalCustomRectangle.setContour(externalContour);
                    externalCustomRectangles.add(externalCustomRectangle);
                }
            }
        }

        /* Look through contours */
        for(MatOfPoint contour : contours) {
            matOfPoint2f.fromList(contour.toList());

            /* Check if current contour has approximately approx.total() vertices. We check for 4 = rectangle. */
            Imgproc.approxPolyDP(matOfPoint2f, approx,Imgproc.arcLength(matOfPoint2f, true) * accuracy, true);

            long total = approx.total();

            /* If 4 vertices are found, the contour is approximately a rectangle */
            if(total == 4) {

                /* Create a boundingRect from the current contour. A boundingRect is the smallest
                 * possible rectangle in which the contour will fit */
                Rect rect = Imgproc.boundingRect(contour);

                /* Find the centerpoints and area */
                double centerX = rect.x + (rect.width / 2);
                double centerY = rect.y + (rect.height / 2);
                double rectArea = rect.width * rect.height;

                boolean skip = false;
                for(Rect erect : externalRects) {
                    if (rect == erect) {
                        skip = true;
                    }
                } if(skip) {
                    continue;
                }

                /* Keep for now, might need
                double diagonal = Math.sqrt(Math.pow(rect.width, 2) + Math.pow(rect.height, 2));
                double aspectRatio = rect.height / rect.width; */

                /* If the rect area is over 3000 we want to draw it
                 * Might need to add more requirements */
                if(rectArea > 3000 /* && aspectRatio > 0.2 /* && rect.width > 50 && rect.height > 100 */) {
                    /*** System.out.print("Rectangle detected. ");
                    System.out.print("Coordinates: " + "(" +centerX + ", " + centerY + ") ");
                    System.out.print("Width: " + rect.width + ", Height: " + rect.height); ***/

                    /* Small QR rects. TODO: Need to detect these from within the larger rect area instead */
                    for(int j = 0; j < externalRects.size(); j++) {
                        Rect erect = externalRects.get(j);

                        if(rect == erect) {
                            continue;
                        }

                        if(rect.x > erect.x && rect.width < erect.width && rect.y > erect.y && rect.height < erect.height) {
                           /*** System.out.println(" | INTERNAL"); ***/
                            externalCustomRectangles.get(j).addChild(1);
                            Imgproc.rectangle(imgcol, rect.br(), rect.tl(), RED, 3, 8, 0);
                        } else {
                           /***  System.out.println(" | NOT INTERNAL "); **/
                        }
                   }
                }
            }
        }

        findBiggestQRCode(imgcol);

        return imgcol;
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
        for(ExternalRectangle e : externalCustomRectangles) {
            if(e.getChildren() >= 2) {
                QRCodes.add(e);
               /*** System.out.println("QR code found!"); ***/
            }
        }

        /* Determine the largest height */
        for(ExternalRectangle e : QRCodes) {
            if(first) {
                biggestQRCode = e.getRect();
                first = false;
            } else if (e.getRect().height > biggestQRCode.height) {
                biggestQRCode = e.getRect();
            }
        }

        if(biggestQRCode != null) {
            // set field
            this.biggestQRCode = biggestQRCode;
//            long time = System.currentTimeMillis();
            Imgproc.rectangle(imgcol, biggestQRCode.br(), biggestQRCode.tl(), NEON_GREEN, 3, 8, 0);
//            System.out.println("Imgproc.rectangle() ran for " + (double) (time-System.currentTimeMillis())/1000 + " seconds");
            count++;
            /*** System.out.println("Biggest QR code is at: " + "(" + e.getRect().x + ", " + e.getRect().y + ")"); ***/
        }
    }

//    /***
//     * Draw a rotated rectangle rRect around the biggest QRCode,f
//     * Find endpoints of rRect,
//     * Compare the bottom left and bottom right points:
//     * If left is lower, drone is to the left of paper
//     * If right is lower, drone is to the right of paper
//     * @param imgcol Mat
//     */
//    public void determinePaperOrientation(Mat imgcol) {
//        MatOfPoint2f approx = new MatOfPoint2f();
//        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
//        MatOfPoint2f boxMop2f = new MatOfPoint2f();
//        List<MatOfPoint> rotatedBox = new ArrayList<>();
//        RotatedRect rRect = null;
//
//        /* Find the object with the biggest QRCode,
//         * then define its rotated rect */
//        for(ExternalRectangle e : QRCodes) {
//            if(e.getRect() == biggestQRCode) {
//                matOfPoint2f.fromList(e.getContour().toList());
//                rRect = Imgproc.minAreaRect(matOfPoint2f);
//            }
//        }
//
//        /* Take the vertices of rRect and store in vertices[], then make a rotatedBox
//         * from by drawing the contour. Use approxPolyDP() to approximate the endpoints,
//         * and store them in a list. */
//         TODO: UNCOMMENT CODE
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

    // Tjekker forholdet for den fundne rektangel
    void checkForA4Papir(Rect rect){
        // Er det et A4 papir frontalt foran kameraet skal det være ~ 1:1.5 højde/bredde ratio.
        double ratio = (double) rect.height / (double) rect.width;
        if ( ratio < 1.5 && ratio > 1.3){
            System.out.println("Fandt A4 papir med Width: " + rect.width + ", Heigth: " + rect.height);
        }
    }

    // TODO: Skift værdierne der tjekkes for, så de passer til dronens kameraopløsning
    public Direction findPaperPosition(Rect rect){
        if (rect.x > 0 && rect.x < 426){
            return Direction.LEFT;
        }

        if (rect.x > 426 && rect.x < 854){
            return Direction.CENTER;
        }

        if (rect.x > 854 && rect.x < 1280){
            return Direction.RIGHT;
        }
        return Direction.UNKNOWN;
    }

    /***
     * Needed for drone video feed
     * @param img BufferedImage
     * @return Mat
     * @throws IOException
     */
    public Mat bufferedImageToMat(BufferedImage img) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", outstream);
        outstream.flush();
        return Imgcodecs.imdecode(new MatOfByte(outstream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    /***
     * Needed for drone video feed
     * @param mat Mat
     * @return BufferedImage
     */
    public BufferedImage matToBufferedImage(Mat mat){
        try {
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, mob);
            return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
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
    public Rect getBiggestQRCode() {
        return biggestQRCode;
    }
}
