package dk.localghost.hold17.autonomous_drone.opencv_processing.util;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;

public class Rectangle {
    private int children = 0;
    private int parent;
    private Rect rect;
    private MatOfPoint parentContour;

    public Rectangle(Rect rect) {
        this.rect = rect;
    }

    public int getChildren() {
        return children;
    }

    public void addChild(int increment) {
        this.children += increment;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public MatOfPoint getContour() {
        return parentContour;
    }

    public void setContour(MatOfPoint contour) {
        this.parentContour = contour;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }
}
