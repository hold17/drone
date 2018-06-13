package dk.localghost.hold17.autonomous_drone.opencv_processing;

import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Shape;
import org.opencv.core.Mat;

public class Benchmark {
    private FilterHelper filterHelper;

    public Benchmark(Shape shape) {
        filterHelper = new FilterHelper();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        benchmark(shape);
    }

    private void benchmark(Shape s) {
        int counter = 0;
        Mat img = filterHelper.openFile("13.jpg");
        long startTime;

        if (s == Shape.CIRCLE) {
            CircleFilter circleFilter = new CircleFilter();
            startTime = System.currentTimeMillis();
            while (10000 > System.currentTimeMillis() - startTime) {
                circleFilter.findCircleAndDraw(img);
                counter++;
            }
            System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + " ms.");
            System.out.println("Filter ran " + counter + " times.");
        } else if (s == Shape.RECTANGLE){
            RectangleFilter rectangleFilter = new RectangleFilter();
            startTime = System.currentTimeMillis();
            while (10000 > System.currentTimeMillis() - startTime) {
                rectangleFilter.filterImage(filterHelper.matToBufferedImage(img));
                counter++;
            }
            System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + " ms.");
            System.out.println("Filter ran " + counter + " times.");
        } else {
            System.err.println("Shape not found!");
        }
    }

    public static void main(String[] args) {
        new Benchmark(Shape.CIRCLE);
    }

}