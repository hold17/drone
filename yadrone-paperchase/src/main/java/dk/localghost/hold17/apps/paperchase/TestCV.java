package dk.localghost.hold17.apps.paperchase;

import org.opencv.core.*;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.opencv.imgproc.Imgproc;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;

public class TestCV {
    public Mat testMat;
    public Mat grayImage;

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public TestCV() {
        try {
            testMat = openFile("a4-papir.jpg");
            System.out.println(testMat.toString());
//            convertGreyscale(testMat);
//            saveFile("imageOutput.jpg", grayImage);
//            detectWhiteMat();
            drawContours();
        } catch (Exception e) {
            System.err.println("Something went wrong: " + e.toString());
        }
    }

    public static void main(String[] args) {
        new TestCV();
    }

    public Mat openFile(String fileName) throws Exception {
        final String path = Paths.get("").toAbsolutePath().toString();
        final String filePath = (path + "/TestImages/" + fileName).replace('/', '\\');

        Mat newImage = Imgcodecs.imread(filePath);
        if (newImage.dataAddr() == 0) {
            throw new Exception("Couldn't open file " + filePath);
        }

        return newImage;
    }

    public Mat bufferedImageToMat(BufferedImage img) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", outstream);
        outstream.flush();
        return Imgcodecs.imdecode(new MatOfByte(outstream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    public BufferedImage matToBufferedImage(Mat mat) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

    public void saveFile(String fileName, Mat testMat) {
        final String path = Paths.get("").toAbsolutePath().toString();
        final String filePath = (path + "/TestImages/" + fileName).replace('/', '\\');
        Imgcodecs.imwrite(filePath, testMat);
        System.out.println("File saved to " + filePath);
    }

//    public void convertGreyscale(Mat testMat) {
//        grayImage = new Mat();
//        cvtColor(testMat, this.grayImage, COLOR_BGR2GRAY);
//    }

    public void detectWhiteIplImage() {

        IplImage img1, imghsv, imgbin;

        img1 = cvLoadImage("TestImages/a4-papir.jpg");
        imghsv = cvCreateImage(cvGetSize(img1), 8, 3);
        imgbin = cvCreateImage(cvGetSize(img1), 8, 1);

        cvCvtColor(img1, imghsv, CV_BGR2HSV);
        CvScalar minc = cvScalar(200, 200, 200, 0);
        CvScalar maxc = cvScalar(255, 255, 255, 0);
        cvInRangeS(img1, minc, maxc, imgbin);

        cvShowImage("default", img1);
        cvShowImage("Binary", imgbin);
        cvWaitKey();

        cvReleaseImage(imghsv);
        cvReleaseImage(imgbin);
        cvReleaseImage(img1);

    }

    public Mat detectWhiteMat() {
        Mat img1, imgbin /* imghsv til konvertering til HSV. Bliver ikke gjort nu */ ;

        try {
            img1 = openFile("realFilterTest.jpg");
            imgbin = new Mat();
            Core.inRange(img1, new Scalar(230, 230, 230, 0), new Scalar(255, 255, 255, 0), imgbin);
//            saveFile("realFilterTestOutput.jpg", imgbin);
            return imgbin;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Mat drawContours() {
        Mat imgbin = detectWhiteMat();
        Mat imgcol = new Mat();
        Mat hierarchy = new Mat();
        imgbin.convertTo(imgbin, CV_8UC3);
        cvtColor(imgbin, imgcol, CV_GRAY2RGB);
//        imgbin.convertTo(imgbin, CvType.CV_32SC1);
        List<MatOfPoint> contours = new ArrayList<>();
        Scalar color = new Scalar(20, 255, 57);
        Imgproc.findContours(imgbin, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        Imgproc.drawContours(imgbin, contours, -1, new Scalar(255, 255, 0), 1);

//        Mat contourImg = new Mat(imgbin.size(), imgbin.type());

//        for (int i = 0; i < contours.size(); i++) {


            Imgproc.drawContours(imgcol, filterContours(contours, 4, 0.5), -1, color, 2, 8, hierarchy, 2, new Point());
//        }

        saveFile("realFilterTestOutputContour.jpg", imgcol);

        return imgcol;
    }

    public List<MatOfPoint> filterContours(List<MatOfPoint> contours, int vertices, double accuracy) {
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();

        List<MatOfPoint2f> contoursMat2 = new ArrayList<>();

        for(int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            matOfPoint2f.fromList(contour.toList());
            Imgproc.approxPolyDP(matOfPoint2f, approx,Imgproc.arcLength(matOfPoint2f, true) * accuracy, true);

            long total = approx.total();

            if(total != vertices) {
                contours.remove(i);
            }
        }

        return contours;

//        for (int i = 0; i < contours.size(); i++) {
//            // Skip small or non-convex objects
//            if (Math.abs(Imgproc.contourArea(contours.get(i))) < 100)
//                continue;
//
//            // Approximate contour with accuracy proportional to the contour perimeter
//            contoursMat2.add(i, (new MatOfPoint2f(contours.get(i))));
//
//            Imgproc.approxPolyDP(contoursMat2.get(i), approx, Imgproc.arcLength(contoursMat2.get(i), true) * 0.02, true);
//
//        }
//
//        for (int i = 0; i < contoursMat2.size(); i++) {
//            contours.add(new MatOfPoint(contoursMat2.get(i)));
//        }
    }
}
