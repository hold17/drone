package dk.localghost.hold17.autonomous_drone.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class QRCodeScanner {
    private ArrayList<TagListener> listener = new ArrayList<>();
    private Result scanResult;
    private long imageCount = 0;

    public void lookForQRCode(BufferedImage image) {
        if ((++imageCount % 2) == 0)
            return;

        // try to detect QR code
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        // decode the barcode (if only QR codes are used, the QRCodeReader might be a better choice)
        QRCodeReader reader = new QRCodeReader();

        double theta = Double.NaN;
        try {
            scanResult = reader.decode(bitmap);

            ResultPoint[] points = scanResult.getResultPoints();
            ResultPoint a = points[1]; // top-left
            ResultPoint b = points[2]; // top-right

            // Find the degree of the rotation (needed e.g. for auto control)

            double z = Math.abs(a.getX() - b.getX());
            double x = Math.abs(a.getY() - b.getY());
            theta = Math.atan(x / z); // degree in rad (+- PI/2)

            theta = theta * (180 / Math.PI); // convert to degree

            if ((b.getX() < a.getX()) && (b.getY() > a.getY())) { // code turned more than 90 clockwise
                theta = 180 - theta;
            } else if ((b.getX() < a.getX()) && (b.getY() < a.getY())) { // code turned more than 180 clockwise
                theta = 180 + theta;
            } else if ((b.getX() > a.getX()) && (b.getY() < a.getY())) { // code turned more than 270 clockwise
                theta = 360 - theta;
            }
        } catch (ReaderException e) {
            // no code found.
            scanResult = null;
        }

        // inform all listeners
        for (int i = 0; i < listener.size(); i++) {
            listener.get(i).onTag(scanResult, (float) theta);
        }
    }

    public void addListener(TagListener listener) {
        this.listener.add(listener);
    }

    public void removeListener(TagListener listener) {
        this.listener.remove(listener);
    }
}
