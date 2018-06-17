package dk.localghost.hold17.autonomous_drone.controller;

public enum FlightControllerType {
    ZX_QR(0), OpenCV_QR(1), CircleDetection(2);

    private final int id;

    FlightControllerType(int id) {
        this.id = id;
    }

    public int getValue() { return id;}
}
