package dk.localghost.hold17.autonomous_drone.controller;

import com.google.zxing.Result;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;

public class QRScannerController implements TagListener, FlightController {

    private String lastScan = null;
    private Direction qrDirection = Direction.UNKNOWN;
    private Direction lastKnownQrDirection = Direction.UNKNOWN;

    private static final int MARGIN = 0;

    @Override
    public void onTag(Result result, float orientation) {
        final int CAMERA_HALF_WIDTH = DroneController.cameraWidth / 2;

        if (result == null) {
            return;
        }

        final double X = result.getResultPoints()[0].getX();
        final double Y = result.getResultPoints()[0].getY();

        // Center = 640
        if (X < CAMERA_HALF_WIDTH - MARGIN)
            qrDirection = Direction.LEFT;
        else if (X > CAMERA_HALF_WIDTH + MARGIN)
            qrDirection = Direction.RIGHT;
        else
            qrDirection = Direction.CENTER;

        lastKnownQrDirection = qrDirection;

        lastScan = result.getText();

        System.out.println("QR Scanned, Result: " + lastScan + " at " + X + ", " + Y + "\tDirection: " + qrDirection);
    }

    public String getLastScan() {
        return lastScan;
    }

    public void resetLastScan() {
        lastScan = null;
    }

    @Override
    public Direction getFlightDirection() {
        return qrDirection;
    }

    @Override
    public Direction getLastKnownDirection() {
        return lastKnownQrDirection;
    }

    @Override
    public void resetFlightDirection() {
        qrDirection = Direction.UNKNOWN;
        resetLastScan();
    }
}
