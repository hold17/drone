package dk.localghost.hold17.autonomous_drone.controller;

import com.google.zxing.Result;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;

public class QRScannerController implements TagListener, QrTracker {

    private String lastScan = null;
    private Direction qrDirection = Direction.UNKNOWN;
    private Direction lastKnownQrDirection = Direction.UNKNOWN;

    @Override
    public void onTag(Result result, float orientation) {
        if (result == null) {
            return;
        }

        final double X = result.getResultPoints()[0].getX();
        final double Y = result.getResultPoints()[0].getY();

        qrDirection = Direction.findXDirection(X);

        if (qrDirection != Direction.UNKNOWN)
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
