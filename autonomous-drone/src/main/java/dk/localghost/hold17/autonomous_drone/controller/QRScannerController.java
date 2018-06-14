package dk.localghost.hold17.autonomous_drone.controller;

import com.google.zxing.Result;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;

public class QRScannerController implements TagListener, QrTracker {

    private String lastScan = null;
    private Direction qrDirection = Direction.UNKNOWN;
    private Direction lastKnownQrDirection = Direction.UNKNOWN;

    // Distance between the to top points of the qr code
    private double horizontalTopDistance = 0;

    @Override
    public void onTag(Result result, float orientation) {
        if (result == null) {
            return;
        }

        double topLeftX = result.getResultPoints()[1].getX();
        double topLeftY = result.getResultPoints()[1].getY();

        double topRightX = result.getResultPoints()[2].getX();
        double topRightY = result.getResultPoints()[2].getY();

        double horizontalTopDistance = topRightX - topLeftX;
        double verticalTopDistance   = topRightY - topRightX;

        this.horizontalTopDistance = horizontalTopDistance;

        qrDirection = Direction.findXDirection(topLeftX);

        if (qrDirection != Direction.UNKNOWN)
            lastKnownQrDirection = qrDirection;

        lastScan = result.getText();

        System.out.println("QR Scanned, Result: " + lastScan + " at " + topLeftX + ", " + topLeftY + "\tDirection: " + qrDirection);
        System.out.println("Top Horizontal Distance: " + horizontalTopDistance);
        System.out.println("Top Vertical Distance  : " + verticalTopDistance);
        System.out.println("--------------------------------");
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
        horizontalTopDistance = 0;
        resetLastScan();
    }

    @Override
    public boolean readyForFlyingThroughRing() {
        return horizontalTopDistance > 170;
    }
}
