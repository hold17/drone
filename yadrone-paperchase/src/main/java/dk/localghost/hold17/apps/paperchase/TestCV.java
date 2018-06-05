package dk.localghost.hold17.apps.paperchase;

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

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.opencv.imgproc.Imgproc.*;

public class TestCV {

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public TestCV() {
        try {
            /* filterImage() runs detectWhiteMat(), then finds contours and runs drawRectangles() */
            filterImage();
        } catch (Exception e) {
            System.err.println("Something went wrong: " + e.toString());
        }
    }

    public static void main(String[] args) {
        new TestCV();
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
            img = openFile("unfiltered.jpg");
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
     *   Convert image to 8-bit then BGR,
     *   Run drawRectangles(),
     * @return Mat
     */
    public Mat filterImage() {
        Mat imgbin = detectWhiteMat();
        Mat imgcol = new Mat();
        Mat hierarchy = new Mat();

        //Denoise binary image using medianBlur and OPEN
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.medianBlur(imgbin, imgbin, 5);
        Imgproc.morphologyEx(imgbin, imgbin, Imgproc.MORPH_OPEN, kernel);

        //Contours are matrices of points. We store all of them in this list.
        List<MatOfPoint> contours = new ArrayList<>();

        /* Convert the binary image to an 8-bit image,
         * then convert to an RGB image so rectangles can be outlined in color */
        imgbin.convertTo(imgbin, CV_8UC3);
        cvtColor(imgbin, imgcol, CV_GRAY2RGB);

        /* Find contours in the binary image. RETR_TREE creates a perfect hierarchy of contours,
         * including children, grandchildren, parents and grandparents. Might be useful later, but
         * is not currently used. Use RETR_EXTERNAL if you only want to find parent contours. */
        Imgproc.findContours(imgbin, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //Detect and draw rectangles on RGB image
        imgcol = drawRectangles(imgcol,contours, hierarchy,0.06);
        saveFile("filtered.jpg", imgcol);

        return imgcol;
    }

    /*** Use contours to detect vertices,
     *   Check for all contours with approximately 4 vertices (square),
     *   Surround squares with a boundingrect,
     *   Check for boundingrect size,
     *   Draw desired boundingrects
     * @param imgcol Mat
     * @param contours List<MatOfPoint>
     * @param hierarchy Mat (not currently in use, might need)
     * @param accuracy double
     * @return Mat
     */
    public Mat drawRectangles(Mat imgcol, List<MatOfPoint> contours, Mat hierarchy, double accuracy) {
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        Scalar color = new Scalar(20, 255, 57);
        Scalar childColor = new Scalar (0, 0, 255);

        /* Look through contours */
        for(int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            matOfPoint2f.fromList(contour.toList());

            /* Check if current contour has approximately approx.total() vertices. We check for 4 = square. */
            Imgproc.approxPolyDP(matOfPoint2f, approx,Imgproc.arcLength(matOfPoint2f, true) * accuracy, true);

            long total = approx.total();

            /* If 4 vertices are found, the contour is approximately a square */
            if(total == 4) {

                /* Create a boundingRect from the current contour. A boundingRect is the smallest
                 * possible rectangle in which the contour will fit */
                Rect rect = Imgproc.boundingRect(contour);

                /* Find the centerpoints and area */
                double centerX = rect.x + (rect.width / 2);
                double centerY = rect.y + (rect.height / 2);
                double rectArea = rect.width * rect.height;

                /* Keep for now, might need
                double diagonal = Math.sqrt(Math.pow(rect.width, 2) + Math.pow(rect.height, 2));
                double aspectRatio = rect.height / rect.width; */

                /* If the rect area is over 3000 we want to draw it
                 * Might need to add more requirements */
                if(rectArea > 3000 /* && aspectRatio > 0.2 /* && rect.width > 50 && rect.height > 100 */) {
                    System.out.print("Rectangle detected. ");
                    System.out.print("Coordinates: " + "(" +centerX + ", " + centerY + ") ");
                    System.out.println("Width: " + rect.width + ", Height: " + rect.height);

                    /* Small QR rects. TODO: Need to detect these from within the larger rect area instead */
                    if (rect.width < 100 && rect.height < 100) {
                        Imgproc.rectangle(imgcol, rect.br(), rect.tl(), childColor, 3, 8, 0);
                    }
                    /* Paper rectangles */
                    else {
                        Imgproc.rectangle(imgcol, rect.br(), rect.tl(), color, 4, 8, 0);
                    }
                }
            }
        }

        return imgcol;
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


    /***TEST METHOD ## Detect edges using a threshold ***/
    public Mat detectEdgesThreshold() {
        Mat imgbin = detectWhiteMat();
        threshold(imgbin, imgbin, 127, 255, Imgproc.THRESH_BINARY);
        return imgbin;
    }

    /***TEST METHOD ## Detect edges using canny ***/
    public Mat detectEdgesCanny() {
        Mat imgbin = detectWhiteMat();
        Imgproc.Canny(imgbin, imgbin, 100, 200);
        return imgbin;
    }
}
