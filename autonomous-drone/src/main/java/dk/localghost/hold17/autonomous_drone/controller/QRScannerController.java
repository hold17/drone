package dk.localghost.hold17.autonomous_drone.controller;

import com.google.zxing.Result;
import dk.localghost.hold17.autonomous_drone.opencv_processing.Direction;

public class QRScannerController implements TagListener {

    private String lastScan = "";
    private Direction qrDirection = Direction.UNKNOWN;

    @Override
    public void onTag(Result result, float orientation) {
        final int CAMERA_HALF_WIDTH = DroneController.cameraWidth / 2;

        if (result == null) {
            qrDirection = Direction.UNKNOWN;
            return;
        }

        final double X = result.getResultPoints()[0].getX();
        final double Y = result.getResultPoints()[0].getY();

//        if (!lastScan.equals(result.getText()))
            System.out.println("QR Scanned, Result: " + result.getText() + " at " + X + ", " + Y + "\tDirection: " + qrDirection);
        lastScan = result.getText();

        // Center = 640
        if (X < CAMERA_HALF_WIDTH)
            qrDirection = Direction.LEFT;
        else if (X > CAMERA_HALF_WIDTH)
            qrDirection = Direction.RIGHT;
        else
            qrDirection = Direction.CENTER;
    }

    public Direction getQrDirection() {
        return qrDirection;
    }

    public void resetQrDirection() {
        qrDirection = Direction.UNKNOWN;
    }
}
