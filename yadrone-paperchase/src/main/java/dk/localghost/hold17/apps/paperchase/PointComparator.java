package dk.localghost.hold17.apps.paperchase;

import org.opencv.core.Point;

import java.util.Comparator;

public class PointComparator {

    public PointComparator() {

    }

    public static final Comparator<Point> Y_COORD = new Comparator<Point>() {
        public int compare(Point p1, Point p2) {
            if(p1.y < p2.y)
                return 0;
            else
                return -1;
        }
    };


}
