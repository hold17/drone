package dk.localghost.hold17.autonomous_drone.opencv_processing;

import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.tracking.Tracker;
import org.opencv.tracking.TrackerMedianFlow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.bytedeco.javacpp.opencv_core.CV_PI;
import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;
import static org.opencv.imgproc.Imgproc.*;

// TODO: Clean up unused code
public class ImageProcessorCopy {

    private final Scalar NEON_GREEN = new Scalar(20, 255, 57);
    private final Scalar RED = new Scalar(0, 0, 255);
    private final Scalar YELLOW = new Scalar(0, 255, 255);
    private final Scalar CYAN = new Scalar(255, 255, 0);

    private final Scalar LOWER = new Scalar(0, 0, 0);
    private final Scalar UPPER = new Scalar(150, 150, 150);

    private List<Rect> externalRects = new ArrayList<>();
    private Rect biggestQRCode;

    private String fileName = "4.jpg";
    private String outputName = "4filtered.jpg";
    private String imgNumber = "4";

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    //
    public ImageProcessorCopy() {
        detectSquare();
    }

    public static void main(String[] args) {
        new ImageProcessorCopy();
    }

    public Mat detectSquare() {
        FilterHelper fh = new FilterHelper();
        Mat img = fh.openFile("12.jpg");

        Mat equ = new Mat();
        img.copyTo(equ);
//        Imgproc.blur(equ, equ, new Size(3, 3));

//        Imgproc.cvtColor(equ, equ, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.equalizeHist(equ, img);


        cvtColor(equ, equ, Imgproc.COLOR_BGR2YUV);
        List<Mat> channels = new ArrayList<Mat>();
        split(equ, channels);
        equalizeHist(channels.get(0), channels.get(0));
        merge(channels, equ);
        cvtColor(equ, equ, Imgproc.COLOR_YUV2BGR);

        fh.saveFile("Equalized.jpg", equ);
        fh.saveFile("EqualizedNOT.jpg", img);
//
        Mat img8u = new Mat();
        cvtColor(img, img8u, COLOR_BGR2GRAY);
//
        Mat thresh = new Mat();
        adaptiveThreshold(img8u, thresh, 120, 140, THRESH_OTSU, 2, 3);
//        Core.inRange(img8u, LOWER, UPPER, thresh);
        fh.saveFile("EqualizedThresh.jpg", thresh);
//        Core.inRange(img, LOWER, UPPER, thresh);
//        fh.saveFile("EqualizedNOTThresh.jpg", thresh);

        GaussianBlur(thresh, thresh, new Size(3, 3), 2.0, 2.0);

        fh.saveFile("testGauss.jpg", thresh);
        Mat edges = new Mat();
        Mat kernelBig = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3, 3));
        Mat kernelSmall = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3, 3));
//        int mean = Core.mean(thresh);
        Canny(thresh, edges, 250, 330, 3, false);
//        dilate(edges, edges, kernelBig);
//        erode(edges, edges, kernelSmall);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
//        findContours(edges, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
//        cvtColor(edges, edges, COLOR_GRAY2RGB);
//        drawContours(edges, contours, -1, CYAN, 3);
//        MatOfPoint2f approx = new MatOfPoint2f()





//        List<Vec2f> lines = new Vector<Vec2f>();
        MatOfPoint2f lines = new MatOfPoint2f();
//        HoughLinesP(edges, lines, 1, Math.PI/180, 100, 40, 5);
        HoughLines(edges, lines, 1, CV_PI/700, 100);

        System.out.println("Detected " + lines.rows() + " lines.");
        for (int i = 0; i < lines.rows(); i++) {
            double data[] = lines.get(i, 0);
            double rho1 = data[0];
            double theta1 = data[1];
            double cosTheta = Math.cos(theta1);
            double sinTheta = Math.sin(theta1);
            double x0 = cosTheta * rho1;
            double y0 = sinTheta * rho1;
            Point pt1 = new Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta);
            Point pt2 = new Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta);
            Imgproc.line(img, pt1, pt2, new Scalar(0, 0, 255), 2);
        }

//        List<Point> ll = lines.toList();
//        for(int i = 0; i < lines.size(); i++ )
//        {
//            for(int j = 0; j < lines.rows(); j++)
//            {
//                Vec2f line1 = lines.get(i, 0);
//                Vec2f line2 = lines[j];
//                if(acceptLinePair(line1, line2, Math.PI / 32))
//                {
//                    opencv_core.Point2f intersection = computeIntersect(line1, line2);
//                    intersections.push_back(intersection);
//                }
//            }
//
//        }
//
//
//        return imgbin;
        fh.saveFile("test.jpg", img);
        fh.saveFile("testedges.jpg", edges);
        return img;
    }

    private boolean acceptLinePair(opencv_core.Point2f line1, opencv_core.Point2f line2, float minTheta)
    {
        float theta1 = line1.get(1), theta2 = line2.get(1);

        if(theta1 < minTheta)
        {
            theta1 += Math.PI; // dealing with 0 and 180 ambiguities...
        }

        if(theta2 < minTheta)
        {
            theta2 += Math.PI; // dealing with 0 and 180 ambiguities...
        }

        return abs(theta1 - theta2) > minTheta;
    }


    public opencv_core.Point2f computeIntersect(opencv_core.Point2f line1, opencv_core.Point2f line2)
    {
        Vector<opencv_core.Point2f> p1 = lineToPointPair(line1);
        Vector<opencv_core.Point2f> p2 = lineToPointPair(line2);

        float denom = (p1.get(0).x() - p1.get(1).x())*(p2.get(0).y() - p2.get(1).y()) - (p1.get(0).y() - p1.get(1).y())*(p2.get(0).x() - p2.get(1).x());
        opencv_core.Point2f intersect = new opencv_core.Point2f(((p1.get(0).x()*p1.get(1).y() - p1.get(0).y()*p1.get(1).x())*(p2.get(0).x() - p2.get(1).x()) -
                (p1.get(0).x() - p1.get(1).x())*(p2.get(0).x()*p2.get(1).y() - p2.get(0).y()*p2.get(1).x())) / denom,
                ((p1.get(0).x()*p1.get(1).y() - p1.get(0).y()*p1.get(1).x())*(p2.get(0).y() - p2.get(1).y()) -
                        (p1.get(0).y() - p1.get(1).y())*(p2.get(0).x()*p2.get(1).y() - p2.get(0).y()*p2.get(1).x())) / denom);

        return intersect;
    }

    //Attempt 1
    public Vector<opencv_core.Point2f> lineToPointPair(opencv_core.Point2f line)
    {
        Vector<opencv_core.Point2f> points = new Vector<>();
        float r = line.get(0), t = line.get(1);
        float cos_t = (float) cos(t);
        float sin_t = (float) sin(t);
        double x0 = r*cos_t, y0 = r*sin_t;
        double alpha = 1000;

        float x1 = (float) (x0 + alpha*(-sin_t));
        float y1 = (float) (y0 + alpha*cos_t);
        float x2 = (float) (x0 - alpha*(-sin_t));
        float y2 = (float) (y0 - alpha*cos_t);

        points.add(new opencv_core.Point2f(x1, y1));
        points.add(new opencv_core.Point2f(x2, y2));

        return points;
    }
}
