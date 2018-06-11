package dk.localghost.hold17.autonomous_drone.opencv_processing;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;

public class ExternalRectangle{
    private int children = 0;
    private Rect rect;
    private RotatedRect rRect;
    private MatOfPoint2f approx;
    private MatOfPoint contour;

    public ExternalRectangle(Rect rect) {
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

    public RotatedRect getrRect() {
        return rRect;
    }

    void setrRect(RotatedRect rRect) {
        this.rRect = rRect;
    }

    public MatOfPoint2f getApprox() {
        return approx;
    }

    void setApprox(MatOfPoint2f approx) {
        this.approx = approx;
    }

    MatOfPoint getContour() {
        return contour;
    }

    public void setContour(MatOfPoint contour) {
        this.contour = contour;
    }
}
