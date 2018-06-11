package dk.localghost.hold17.autonomous_drone.opencv_processing.filter;

import dk.localghost.hold17.autonomous_drone.opencv_processing.Shape;
import org.opencv.core.Mat;

public class Benchmark {
    private RectangleFilter rectangleFilter;
    private CircleFilter circleFilter;
    private FilterHelper filterHelper;

    public Benchmark() {
        filterHelper = new FilterHelper();
        rectangleFilter = new RectangleFilter();
        circleFilter = new CircleFilter();
        benchmark(Shape.CIRCLE);
    }

    public void benchmark(Shape s) {
        int counter = 0;
        long startTime = System.currentTimeMillis();
        Mat img = filterHelper.openFile("3.jpg");

        while (10000 > System.currentTimeMillis() - startTime) {
            if (s == Shape.CIRCLE) {
                circleFilter.findCircleAndDraw(img, 1, 150);
            } else if (s == Shape.RECTANGLE) {
                rectangleFilter.filterImage(filterHelper.matToBufferedImage(img));
            } else {
                System.out.println("No valid shape");
            }
            counter++;
        }

        long stopTime = System.currentTimeMillis();
        System.out.println("Total time: " + (double) (stopTime - startTime) / 1000 + " seconds.");
        System.out.println("Program ran " + counter + " times.");
    }
}
