package dk.localghost.hold17.apps.paperchase;

import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

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
            detectWhiteMat();
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
            Core.inRange(img1, new Scalar(220, 220, 220, 0), new Scalar(255, 255, 255, 0), imgbin);
            saveFile("realFilterTestOutput.jpg", imgbin);
            return imgbin;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
