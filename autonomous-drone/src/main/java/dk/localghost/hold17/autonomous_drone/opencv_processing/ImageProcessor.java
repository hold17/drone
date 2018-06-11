package dk.localghost.hold17.autonomous_drone.opencv_processing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.*;

// TODO: Clean up unused code
public class ImageProcessor {

    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar RED = new Scalar(0, 0, 255);
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

    // Global variabel for centrum af største cirkel.
    private Point biggestCircle = new Point();

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public ImageProcessor() {
//        benchmark();
    }

    public static void main(String[] args) {
        new ImageProcessor();
    }

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
        Core.inRange(image, new Scalar(0, 0, 0), new Scalar(200, 200, 200), imgbin);
//        imgbin.convertTo(imgbin, COLOR_BGRA2GRAY);

        return imgbin;
    }

    /*** Denoise binary image,
     *   Find contours in binary image,
     *   Convert image to 8-bit then RGB,
     *   Run drawRectangles(),
     *   Run determinePaperOrientation;
     * @return Mat
     */
    public Mat filterImage(Mat mat) {
        externalCustomRectangles.clear();
        externalRects.clear();
        QRCodes.clear();

        Mat imgbin = detectWhiteMat(mat);
        Mat imgcol = new Mat();
//        Mat hierarchy1 = new Mat();
        Mat hierarchy2 = new Mat();

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
//        Imgproc.medianBlur(imgbin, imgbin, 5);
//        Imgproc.morphologyEx(imgbin, imgbin, Imgproc.MORPH_OPEN, kernel);
        GaussianBlur(imgbin, imgbin, new Size(3, 3), 2.0, 2.0);

//        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> externalContours = new ArrayList<>();

        Mat canny = new Mat();
        Imgproc.Canny(imgbin, canny, 100, 300);
        cvtColor(canny, imgcol, COLOR_GRAY2RGB);
        imgbin.convertTo(imgbin, CvType.CV_8UC3);


//        Imgproc.findContours(imgbin, contours, hierarchy1, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(canny, externalContours, hierarchy2, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

//        imgcol = drawRectangles(imgcol, contours, externalContours, hierarchy1, 0.06);
//        determinePaperOrientation(imgcol);
//        saveFile("filtered.jpg", imgcol);

        List<MatOfInt> hullExt = new ArrayList<>();
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        List<MatOfPoint> realContours = new ArrayList<>();

        for(int i = 0; i < externalContours.size(); i++) {
            MatOfInt temp;
            matOfPoint2f.fromList(externalContours.get(i).toList());
            Imgproc.approxPolyDP(matOfPoint2f, approx, Imgproc.arcLength(matOfPoint2f, true) * 0.05, true);

            if(approx.total() == 4) {
                Imgproc.convexHull(externalContours.get(i), temp = new MatOfInt(), false);
                hullExt.add(temp);
                realContours.add(externalContours.get(i));
            }
        }

        MatOfInt hullInt = new MatOfInt();
        for(MatOfPoint contour : externalContours) {
            Imgproc.convexHull(contour, hullInt, true);
        }

            List<MatOfPoint> hullMat = new ArrayList<>();
        for(int i = 0; i < hullExt.size(); i++) {
            hullMat.add(convertIndexesToPoints(realContours.get(i), hullExt.get(i)));
        }
//        for(int i = 0; i < hullExt.size(); i++) {
//            Imgproc.drawContours(imgcol, contours, i, CYAN, 2, 8, new MatOfPoint(), 0, new Point());
            Imgproc.drawContours(imgcol, hullMat, -1, CYAN, 2);

//        }
        return imgcol;

    }

    public static MatOfPoint convertIndexesToPoints(MatOfPoint contour, MatOfInt indexes) {
        int[] arrIndex = indexes.toArray();
        Point[] arrContour = contour.toArray();
        Point[] arrPoints = new Point[arrIndex.length];

        for (int i=0;i<arrIndex.length;i++) {
            arrPoints[i] = arrContour[arrIndex[i]];
        }

        MatOfPoint hull = new MatOfPoint();
        hull.fromArray(arrPoints);
        return hull;
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

        for (MatOfPoint externalContour : externalContours) {
            matOfPoint2f.fromList(externalContour.toList());

            /* Check if current contour has approximately approx.total() vertices. We check for 4 = rectangle. */
            Imgproc.approxPolyDP(matOfPoint2f, approx, Imgproc.arcLength(matOfPoint2f, true) * accuracy, true);

            long total = approx.total();

            /* If 4 vertices are found, the contour is approximately a rectangle */
            if (total == 4) {
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

                boolean skip = false;
                for (Rect erect : externalRects) {
                    if (rect == erect) {
                        skip = true;
                    }
                }
                if (skip) {
                    continue;
                }

                /* Keep for now, might need
                double diagonal = Math.sqrt(Math.pow(rect.width, 2) + Math.pow(rect.height, 2));
                double aspectRatio = rect.height / rect.width; */

                /* If the rect area is over 3000 we want to draw it
                 * Might need to add more requirements */
                if (rectArea > 3000 /* && aspectRatio > 0.2 /* && rect.width > 50 && rect.height > 100 */) {
                    /*** System.out.print("Rectangle detected. ");
                     System.out.print("Coordinates: " + "(" +centerX + ", " + centerY + ") ");
                     System.out.print("Width: " + rect.width + ", Height: " + rect.height); ***/

                    /* Small QR rects. TODO: Need to detect these from within the larger rect area instead */
                    for (int j = 0; j < externalRects.size(); j++) {
                        Rect erect = externalRects.get(j);

                        if (rect == erect) {
                            continue;
                        }

                        if (rect.x > erect.x && rect.width < erect.width && rect.y > erect.y && rect.height < erect.height) {
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
        for (ExternalRectangle e : externalCustomRectangles) {
            if (e.getChildren() >= 2) {
                QRCodes.add(e);
                /*** System.out.println("QR code found!"); ***/
            }
        }

        /* Determine the largest height */
        for (ExternalRectangle e : QRCodes) {
            if (first) {
                biggestQRCode = e.getRect();
                first = false;
            } else if (e.getRect().height > biggestQRCode.height) {
                biggestQRCode = e.getRect();
            }
        }

        if (biggestQRCode != null) {
            // set field
            this.biggestQRCode = biggestQRCode;
            Imgproc.rectangle(imgcol, biggestQRCode.br(), biggestQRCode.tl(), NEON_GREEN, 3, 8, 0);
            /*** System.out.println("Biggest QR code is at: " + "(" + biggestQRCode.x + ", " + biggestQRCode.y + ")"); ***/
        }
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
     * Needed for drone video feed
     * @param img BufferedImage
     * @return Mat
     */
    public Mat bufferedImageToMat(BufferedImage img) {
//        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
//        try {
//            long start1 = System.currentTimeMillis();
//            ImageIO.write(img, "jpg", outstream);
//            System.out.println("imgwrite took: " + (System.currentTimeMillis()-start1));
//            long start2 = System.currentTimeMillis();
//            outstream.flush();
//            System.out.println("flush took: " + (System.currentTimeMillis()-start2));
//            long start3 = System.currentTimeMillis();
//            MatOfByte matOfByte = new MatOfByte(outstream.toByteArray());
//            System.out.println("matofbyte took: " + (System.currentTimeMillis()-start3));
//            long start4 = System.currentTimeMillis();
//            Mat mat = Imgcodecs.imdecode(matOfByte, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
//            System.out.println("decode took: " + (System.currentTimeMillis()-start4));

//            long start1 = System.currentTimeMillis();
            byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
//            System.out.println("imgwrite took: " + (System.currentTimeMillis()-start1));
//            long start2 = System.currentTimeMillis();
            Mat mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
//            System.out.println("imgwrite took: " + (System.currentTimeMillis()-start2));
//            long start3 = System.currentTimeMillis();
            mat.put(0, 0, data);
//            System.out.println("imgwrite took: " + (System.currentTimeMillis()-start3));

            return mat;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    /***
     * Needed for drone video feed
     * @param mat Mat
     * @return BufferedImage
     */
    public BufferedImage matToBufferedImage(Mat mat) {
//        try {
//            MatOfByte mob = new MatOfByte();
//            Imgcodecs.imencode(".jpg", mat, mob);
//            return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//        long start1 = System.currentTimeMillis();
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
//        System.out.println("imgwrite took: " + (System.currentTimeMillis()-start1));

//        long start2 = System.currentTimeMillis();
        WritableRaster raster = image.getRaster();
//        System.out.println("imgwrite took: " + (System.currentTimeMillis()-start2));

//        long start3 = System.currentTimeMillis();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
//        System.out.println("imgwrite took: " + (System.currentTimeMillis()-start3));

//        long start4 = System.currentTimeMillis();
        byte[] data = dataBuffer.getData();
//        System.out.println("imgwrite took: " + (System.currentTimeMillis()-start4));

//        long start5 = System.currentTimeMillis();
        mat.get(0, 0, data);
//        System.out.println("imgwrite took: " + (System.currentTimeMillis()-start5));

        return image;
    }

    public Rect getBiggestQRCode() {
        return biggestQRCode;
    }

    private void benchmark(/* Shape s*/) {
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        int counter = 0;
        long startTime = System.currentTimeMillis();
        Mat img = openFile("4.jpg");

        while (10000 > System.currentTimeMillis() - startTime) {
//            if (s == Shape.CIRCLE) {
//                findCircleAndDraw(img, 1, 150);
//            } else if (s == Shape.RECTANGLE) {
            BufferedImage bufferedImage = matToBufferedImage(img);
            img = bufferedImageToMat(bufferedImage);
                filterImage(img);
//            } else {
//                System.out.println("No valid shape");
//            }
            counter++;
        }

        long stopTime = System.currentTimeMillis();
        System.out.println("Total time: " + (double) (stopTime - startTime) / 1000 + " seconds.");
        System.out.println("Program ran " + counter + " times.");
    }
}