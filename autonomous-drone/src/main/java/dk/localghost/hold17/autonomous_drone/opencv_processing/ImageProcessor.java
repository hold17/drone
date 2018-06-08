package dk.localghost.hold17.autonomous_drone.opencv_processing;

import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
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
import java.util.Collections;
import java.util.List;

import org.opencv.imgproc.Imgproc;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
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

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public ImageProcessor() {
        try {
            /* filterImage() runs detectWhiteMat(), then finds contours and runs drawRectangles() */
            //long startTime = System.currentTimeMillis();
            //int count = 0;
            //while(10000 >  System.currentTimeMillis() - startTime) {
                //filterImage();
              //  count++;
            //System.out.println("Program ran " + count + " times.");
            saveFile("filtered.jpg", filterImage());
        } catch (Exception e) {
            System.err.println("Something went wrong: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ImageProcessor();
    }

    /***
     * Open file as matrix
     * @param fileName file
     * @return Mat
     * @throws Exception new
     */
    public Mat openFile(String fileName) throws Exception {
        final String path = Paths.get("").toAbsolutePath().toString();
        final String filePath = (path + "/TestImages/" + fileName).replace('/', '\\');

        Mat newImage = Imgcodecs.imread(filePath);
        if (newImage.dataAddr() == 0) {
            throw new Exception("Couldn't open file " + filePath);
        }

        return newImage;
    }

    /*** Save matrix to file ***/
    public void saveFile(String fileName, Mat testMat) {
        final String path = Paths.get("").toAbsolutePath().toString();
        final String filePath = (path + "/TestImages/" + fileName).replace('/', '\\');
        Imgcodecs.imwrite(filePath, testMat);
        System.out.println("File saved to " + filePath);
    }

    /***
     * Convert image to a binary matrix and remove everything that is not the desired shade of white
     * @return Mat
     */
    public Mat detectWhiteMat() {
        Mat img, imgbin;

        try {
            img = openFile("4.jpg");
            imgbin = new Mat();

            /* Write 1 if in range of the two scalars, 0 if not. Binary image result written to imgbin */
            Core.inRange(img, new Scalar(225, 225, 225, 0), new Scalar(255, 255, 255, 0), imgbin);

            return imgbin;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*** Denoise binary image,
     *   Find contours in binary image,
     *   Convert image to 8-bit then RGB,
     *   Run drawRectangles(),
     *   Run determinePaperOrientation;
     * @return Mat
     */
    public Mat filterImage() {
        Mat imgbin = detectWhiteMat();
        Mat imgcol = new Mat();
        Mat hierarchy1 = new Mat();
        Mat hierarchy2 = new Mat();


        // Forsøger at finde cirkler
        Mat mask = new Mat();
        Mat img = null;
        try {
            img = openFile("4.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        img = FindCircle_v2(img);

        // Tegner fundne cirkler ind i billedet. Virker ikke optimalt
        //cvtColor(img, img, CV_RGB2GRAY);
        //FindCircleAndDraw(img,5,100);
        imgcol = img;



        //Denoise binary image using medianBlur and OPEN
     //   Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
   //     Imgproc.medianBlur(imgbin, imgbin, 5);
      //  Imgproc.morphologyEx(imgbin, imgbin, Imgproc.MORPH_OPEN, kernel);

        //Contours are matrices of points. We store all of them in this list.
        //List<MatOfPoint> contours = new ArrayList<>();
        //List<MatOfPoint> externalContours = new ArrayList<>();

        /* Convert the binary image to an 8-bit image,
         * then convert to an RGB image so rectangles can be outlined in color */
        //imgbin.convertTo(imgbin, CV_8UC3);
        //cvtColor(imgbin, imgcol, CV_GRAY2RGB);

        /* Find contours in the binary image. RETR_TREE creates a perfect hierarchy of contours,
         * including children, grandchildren, parents and grandparents. Might be useful later, but
         * is not currently used. Use RETR_EXTERNAL if you only want to find parent contours. */
        //Imgproc.findContours(imgbin, contours, hierarchy1, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //Imgproc.findContours(imgbin, externalContours, hierarchy2, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Detect and draw rectangles on RGB image
        //imgcol = drawRectangles(imgcol,contours, externalContours, hierarchy1,0.06);
        //determinePaperOrientation(imgcol);
        //saveFile("filtered.jpg", imgcol);


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
                    Imgproc.rectangle(imgcol, rect.br(), rect.tl(), NEON_GREEN, 4, 8, 0);

                    //Create rotated rectangle by defining the minimum area in which the contour will fit
                    RotatedRect rRect = Imgproc.minAreaRect(matOfPoint2f);
                    externalRects.add(rect);
                    externalCustomRectangle = new ExternalRectangle(rect);
                    externalCustomRectangle.setrRect(rRect);
                    externalCustomRectangle.setApprox(approx);
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
        int maxHeight = 0;
        boolean first = true;

        /* Define and find QRCodes as rectangles with 3 or more children */
        for(ExternalRectangle e : externalCustomRectangles) {
            if(e.getChildren() >= 3) {
                QRCodes.add(e);
               /*** System.out.println("QR code found!"); ***/
            }
        }

        /* Determine the largest height */
        for(ExternalRectangle e : QRCodes) {
            if(first) {
                maxHeight = e.getRect().height;
                first = false;
            } else if (e.getRect().height > maxHeight) {
                maxHeight = e.getRect().height;
            }
        }

        /* Find the biggest QR-code by height */
        for(ExternalRectangle e : QRCodes) {
            if (maxHeight == e.getRect().height) {
                biggestQRCode = e.getRect();
                Imgproc.rectangle(imgcol, e.getRect().br(), e.getRect().tl(), NEON_GREEN, 5, 8, 0);
               /*** System.out.println("Biggest QR code is at: " + "(" + e.getRect().x + ", " + e.getRect().y + ")"); ***/

                Imgproc.putText(imgcol, "QR code", new Point(e.getRect().x + 50, e.getRect().y - 100), 4,5, NEON_GREEN);
            }
        }
    }

    /***
     * Draw a rotated rectangle rRect around the biggest QRCode,
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
        for(ExternalRectangle e : QRCodes) {
            if(e.getRect() == biggestQRCode) {
                matOfPoint2f.fromList(e.getContour().toList());
                rRect = Imgproc.minAreaRect(matOfPoint2f);
            }
        }

        /* Take the vertices of rRect and store in vertices[], then make a rotatedBox
         * from by drawing the contour. Use approxPolyDP() to approximate the endpoints,
         * and store them in a list. */
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

            /* Draw all points as filled circles in red */
            for (Point p : approxList) {
                Imgproc.circle(imgcol, new Point(p.x, p.y), 10, RED, FILLED);
            }

            /* If the lowest point is to the right of the second lowest point,
             * the drone is to the right of the paper. Then the opposite.
             */
            if(lower1.x > lower2.x) {
               /*** System.out.println("Lowest point is in the right side of rotated rect at point " + lower1);
                System.out.println("Drone is looking at the QR code from the RIGHT"); ***/
                Imgproc.circle(imgcol, lower1, 15, CYAN, FILLED);
            }
            else if(lower1.x < lower2.x) {
                /*** System.out.println("Lowest point is in the right side of rotated rect at point " + lower1);
                System.out.println("Drone is looking at the QR code from the LEFT"); ***/
                Imgproc.circle(imgcol, lower1, 15, CYAN, FILLED);
            }
        }
    }

    // Tjekker forholdet for den fundne rektangel
    void TjekForA4Papir(Rect rect){
        // Er det et A4 papir frontalt foran kameraet skal det være ~ 1:1.5 højde/bredde ratio.
        double ratio = (double) rect.height / (double) rect.width;
        if ( ratio < 1.5 && ratio > 1.3){
            System.out.println("Fandt A4 papir med Width: " + rect.width + ", Heigth: " + rect.height);
        }
    }



    // TODO: Skift værdierne der tjekkes for, så de passer til dronens kameraopløsning
    String FindPaperPosition(Rect rect){
        if (rect.x > 0 && rect.x < 1533){
            return "Til venstre\n";
        }

        if (rect.x > 1533 && rect.x < 3066){
            return "I centrum\n";
        }

        if (rect.x > 3066 && rect.x < 4640){
            return "Til højre\n";
        }
        return "Papirets position blev ikke fundet\n";
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
     * @throws IOException
     */
    public BufferedImage matToBufferedImage(Mat mat) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }


    // 150 - 255 virker til at sortere et nogenlunde tydeligt billede af de røde cirkler
    /***TEST METHOD ## Detect edges using a threshold ***/
    public Mat detectEdgesThreshold(Mat img) {
        threshold(img, img, 150, 255, Imgproc.THRESH_BINARY);
        return img;
    }

    /***TEST METHOD ## Detect edges using canny ***/
    public Mat detectEdgesCanny(Mat img, int t1, int t2) {
        Mat detected_edges = new Mat();
        blur(img, detected_edges, new Size(3,3) );

        //Imgproc.Canny(detected_edges, detected_edges, t1, t2);
        return detected_edges;
    }


    /*
     * Funktion der finder cirkler samt tegner dem
     * dp = pixelreduktionsgrad
     * minDist = minimum radius i pixelantal
     * TODO: Udvid funktion således at det returneres om cirklen er til henholdsvis venstre/højre/centrum af billedet. Kig på Point koordinater.
     *
     * PROBLEM: Finder mange cirkler, også hvor der ikke giver mening.
     */

    void FindCircleAndDraw(Mat image, int dp, int minDist){
        // finder circler med Hough Transform

        //Parameter tjek
        if (dp <= 0)  dp = 1;
        if (minDist <= 0) minDist = 1;


        Mat circlePosition = new Mat();
        // finder objekter der ligner cirkler og gemmer deres position i circlePosition
        Imgproc.HoughCircles(image, circlePosition, Imgproc.CV_HOUGH_GRADIENT, dp, minDist );

        // fortsæt kun, hvis der er fundet én eller flere cirkler
        if (circlePosition.empty() == false)
        {
            System.out.println("Fandt: " + circlePosition.cols() + " cirkel(er)");

            // sætter cirklens farve
            double farve = 100;
            Scalar color = new Scalar(farve);

            for (int i = 0; i < circlePosition.cols(); i++) // antallet af kolonner angiver antallet af cirkler fundet
            {
                double[] testArr = circlePosition.get(0,i);
                System.out.println("\nCirkel nr. " + i + " fundet på:\nx-koord: " + testArr[0] + "\ny-koord: " + testArr[1] + "\nradius: " + testArr[2]);


                // sætter cirklens centrum
                Point center = new Point(testArr[0], testArr[1]);

                // parser radius til int for at efterleve parametre krav i circle()
                double radiusDouble = testArr[2];
                int radius = (int) radiusDouble;
                // tegner cirklen
                Imgproc.circle(image, center, radius, color);
            }
        } else{
            System.out.println("Der blev ikke fundet nogle cirkler i billedet");
        }

    }


    /*
    * Ikke færdig.
    *
    * Skal finde cirkel, lige nu forsøges med diverse metoder for at se hvad virker bedst
     */
    Mat FindCircle_v2(Mat img){
        //Sorterer vise farver fra, så vi ender med tydeligere cirkler
        img = detectEdgesThreshold(img);

        // Fjerner farver, så vi kun har røde farver tilbage
        Scalar lower_red = new Scalar(50,50,110);
        Scalar upper_red = new Scalar(255,255,130);
        //Core.inRange(img, lower_red, upper_red, img);


        // Find kanter med canny()
        int threshold1 = 100;
        int threshhold2 = 200;
        //img = detectEdgesCanny(img, threshold1, threshhold2); // denne funk virker kun halvt

        return img;

    }
}
