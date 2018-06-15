package dk.localghost.hold17.autonomous_drone.controller;

import dk.localghost.hold17.autonomous_drone.opencv_processing.CircleFilter;
import dk.localghost.hold17.autonomous_drone.opencv_processing.util.Direction;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.CommandManager;
import dk.localghost.hold17.base.command.LEDAnimation;
import dk.localghost.hold17.base.navdata.Altitude;
import dk.localghost.hold17.base.navdata.AltitudeListener;
import dk.localghost.hold17.base.navdata.BatteryListener;
import dk.localghost.hold17.base.utils.ConsoleColors;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DroneController {
    private IARDrone drone;
    private CommandManager cmd;
    private QRCodeScanner qrScanner;
    private QRScannerController qrController;

    private int currentFlightController = 0;
    private List<FlightController> flightControllers = new ArrayList<>();

    private static CircleFilter circleFilter = new CircleFilter();

    private final static int MAX_ALTITUDE = 1400;
    private final static int MIN_ALTITUDE = 900;
    private int droneAltitude = 0;
    private int droneBattery = 0;
    private boolean droneFlying = false;
    private static int speed;
    public static int cameraWidth = 1280;
    public static int cameraHeight = 720;

    public DroneController(IARDrone drone, int speed) {
        this.drone = drone;
        this.cmd = this.drone.getCommandManager();
        this.speed = speed;
        initializeDrone();
    }

    private void initializeDrone() {
        drone.start();

        drone.setMinAltitude(MIN_ALTITUDE); // TODO Doesn't work..
        drone.setMaxAltitude(4000);
        initializeListeners();

        System.out.println(ConsoleColors.BLUE_BRIGHT + "CURRENT BATTERY: " + droneBattery + "%" + ConsoleColors.RESET);

        if (droneBattery < 20) {
            System.out.println(ConsoleColors.YELLOW_BOLD_BRIGHT + "WARNING: Battery percentage low (" + droneBattery + "%)!" + ConsoleColors.RESET);
        }

        qrController = new QRScannerController();
        qrScanner = new QRCodeScanner();
        qrScanner.addListener(qrController);

        qrScanner.addListener(qrController);
        flightControllers.add(qrController);

        writeFlightController();

        LEDSuccess();
    }

    public void updateQR(BufferedImage bufferedImage) {
        qrScanner.lookForQRCode(bufferedImage);
    }

    public CircleFilter getCircleFilter() {
        return circleFilter;
    }

    private void initializeListeners() {
        drone.getNavDataManager().addAltitudeListener(new AltitudeListener() {
            @Override
            public void receivedAltitude(int altitude) {
                droneAltitude = altitude;
            }

            @Override
            public void receivedExtendedAltitude(Altitude d) {

            }
        });

        drone.getNavDataManager().addBatteryListener(new BatteryListener() {
            @Override
            public void batteryLevelChanged(int percentage) {
                droneBattery = percentage;
            }

            @Override
            public void voltageChanged(int vbat_raw) {

            }
        });
    }

    /**
     * Takes of or lands depending of the current state of the drone
     */
    public void takeoffOrLand() {
        if (drone == null) return;

        if (!droneFlying) {
            cmd.takeOff();
            cmd.after(1000).hover();
            droneFlying = true;
        } else {
            cmd.hover().doFor(100);
            cmd.landing();
            droneFlying = false;
        }
    }

    /**
     * Interrupts any thread (i think)
     */
    public void interrupt() {
        cmd.stop();
    }

    public void hover() {
        // TODO: Test if doFor is really necessary here
        cmd.hover().doFor(100);
    }

    /**
     * Interrupts and resets the drone. Emergency land
     */
    public void reset() {
        drone.reset();
        cmd.flatTrim();
    }

    /**
     * Calls drone.stop()
     */
    public void stop() {
        drone.stop();
    }

    /**
     * Autonomous flight: up, forward, down
     */
    public void flyThroughRing() {
        cmd.hover().doFor(250);
        goToDetectionAltitude();
        cmd.hover().doFor(250);
        // Change this value to change the distance to fly when flying through rings
        final int FORWARD_TIME = 1000;

        // UP
        System.out.println("          FLYING UP");
        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 1);
        goToRingAltitude();

        // WAIT
        cmd.hover().doFor(100);

        // FORWARD
        System.out.println("          FLYING FORWARD");
        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 1);
        cmd.forward(speed).doFor(FORWARD_TIME);

        // WAIT
        cmd.hover().doFor(250);

        // DOWN
        System.out.println("          FLYING DOWN");
        goToDetectionAltitude();

        // WAIT
        cmd.hover().doFor(250);
    }

    /**
     * Goes to the altitude needed for detecting a direction. For a QrTracker it is the minimum altitude, for a
     * CircleTracker it is the maximum altitude.
     */
    public void goToDetectionAltitude() {
        if (getCurrentFlightController() instanceof QrTracker) {
            goToAltitude(MIN_ALTITUDE);
        } else if (getCurrentFlightController() instanceof CircleTracker) {
            goToAltitude(MAX_ALTITUDE);
        }
    }

    /**
     * Goes to the altitude needed for flying through the ring. This is always the maximum altitude.
     */
    public void goToRingAltitude() {
        goToAltitude(MAX_ALTITUDE);
    }

    /**
     * Goes up or down to a specified altitude
     *
     * @param altitude  the altitude to specify
     */
    private void goToAltitude(int altitude) {
        if (droneAltitude < MAX_ALTITUDE) {
            while(droneAltitude < altitude) {
                cmd.up(speed / 2).doFor(250);
            }
        } else {
            while(droneAltitude > altitude) {
                cmd.down(speed / 2).doFor(250);
            }
            while(droneAltitude < altitude) {
                cmd.up(speed / 4).doFor(100);
            }
        }
    }


//    public void bum() {
//        final Direction paperDirection = getPaperDirection();
//
//        switch (paperDirection) {
//            case LEFT:
//                System.out.println("Left"); break;
//            case RIGHT:
//                System.out.println("Right"); break;
//            case CENTER:
//                System.out.println("Center"); break;
//            case UNKNOWN:
//                System.out.println("Unknown"); break;
//        }
//    }


//    public void alignCircle() {
//        Direction directionToCircleCenter = null;
////        Remove this line of code if testing on table.
////        goToRingAltitude();
//        System.out.println("IM AT THE TOP");
//        while (directionToCircleCenter != Direction.CENTER) {
//            Direction tempDirection = Direction.findXDirection(circleFilter.getBiggestCircle().x); // henter enum ud fra fundne stoerste cirkel
//            if (tempDirection != Direction.UNKNOWN) {
//                directionToCircleCenter = tempDirection;
//            }
//
//            System.out.println(ConsoleColors.WHITE_UNDERLINED + ConsoleColors.GREEN + "CIRCLE IS TO THE " + directionToCircleCenter + ConsoleColors.RESET);
//            if (directionToCircleCenter != null) {
//                switch (directionToCircleCenter) {
//                    case LEFT:
//                    case LEFTDOWN:
//                    case LEFTUP:
//                        cmd.setLedsAnimation(LEDAnimation.BLINK_RED, 6, 1);
//                        cmd.goLeft(speed).doFor(500);
//                        break;
//                    case RIGHT:
//                    case RIGHTUP:
//                    case RIGHTDOWN:
//                        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 6, 1);
//                        cmd.goRight(speed).doFor(500);
//                        break;
//                    case DOWN:
//                    case UP:
//                    case CENTER:
//                        LEDSuccess();
//                        cmd.forward(speed).doFor(500);
//                        System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + "Found circle" + ConsoleColors.RESET);
//                        break;
//                }
//            }
//            cmd.hover();
//            cmd.waitFor(1000);
//        }
//
//        cmd.setLedsAnimation(LEDAnimation.SNAKE_GREEN_RED, 1, 10);
////        drone.landing();
//    }

//    private Direction getPaperDirection() {
//        RectangleFilter rectangleFilter = new RectangleFilter();
//
//        rectangleFilter.findBiggestQRCode(rectangleFilter.filterImage(droneCamera));
//        return rectangleFilter.findPaperPosition(rectangleFilter.getBiggestQRCode());
//    }

    public void alignTarget() {
        Direction targetDirection = Direction.UNKNOWN;

        goToDetectionAltitude();

        for (int i = 0; i < 25; i++) {
             targetDirection = getCurrentFlightController().getFlightDirection();

             if (getCurrentFlightController().readyForFlyingThroughRing()) {
                 System.out.println(ConsoleColors.YELLOW_BRIGHT + "Ready to fly through the ring!!!" + ConsoleColors.RESET);
                 flyThroughRing();
                 cmd.hover();
//                 cmd.landing();
                 return;
             }

            switch (targetDirection) {
                case LEFT:
                    cmd.goLeft(speed).doFor(250);
                    break;
                case RIGHT:
                    cmd.goRight(speed).doFor(250);
                    break;
                case CENTER:
                    cmd.forward(speed).doFor(350);
                    break;
                case UNKNOWN:
                    flyToLastKnownDirection();
                    searchForLostTarget(2);
                    qrController.resetLastScan();
                    break;
            }

            getCurrentFlightController().resetFlightDirection();
            cmd.hover().waitFor(2000);

            if (getCurrentFlightController().readyForFlyingThroughRing()) break;
        }

        if (getCurrentFlightController().readyForFlyingThroughRing()) {
            System.out.println(ConsoleColors.YELLOW_BRIGHT + "Ready to fly through the ring!!! 2nd time" + ConsoleColors.RESET);
//            cmd.spinLeft(100).doFor(250).hover().waitFor(250);
//            cmd.spinRight(100).doFor(250);
            flyThroughRing();
            cmd.hover();
//            cmd.landing();
        }
    }

    private void flyToLastKnownDirection() {
        switch (getCurrentFlightController().getLastKnownDirection()) {
            case LEFT:
                cmd.goLeft(speed).doFor(250);
                break;
            case RIGHT:
                cmd.goRight(speed).doFor(250);
                break;
            case CENTER:
                cmd.forward(speed).doFor(350);
                break;
        }
    }

    /**
     * Makes minor adjustments to find the target that was recently lost
     *
     * @param searchCount  how many iterations the search algorithm should execute
     */
    private void searchForLostTarget(int searchCount) {
        final int FLY_SPEED = speed / 2;
        final int FLY_TIME = 300;
        final int WAIT_TIME = 2500;
        final int TEST_COUNT = 3;

        for (FlightController fc : flightControllers) {
            for (int i = 0; i < searchCount; i++) {
                cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 1);
                cmd.backward(FLY_SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
                if (targetFound(fc)) return;

                testLeft(FLY_SPEED, FLY_TIME, WAIT_TIME, TEST_COUNT);
                if (targetFound(fc)) return;
                testRight(FLY_SPEED, FLY_TIME, WAIT_TIME, TEST_COUNT);
                if (targetFound(fc)) return;
            }

            takeoffOrLand();
        }
    }

    private void testLeft(final int SPEED, final int FLY_TIME, final int WAIT_TIME, final int COUNT) {
        int resetFlyCount = 0;

        for (int i = 0; i < COUNT; i++) {
            cmd.goLeft(SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
            resetFlyCount = i + 1;

            if (targetFound(getCurrentFlightController())) return;
        }

        for (int i = 0; i < resetFlyCount; i++) {
            cmd.goRight(SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
        }
    }

    private void testRight(final int SPEED, final int FLY_TIME, final int WAIT_TIME, final int COUNT) {
        int resetFlyCount = 0;

        for (int i = 0; i < COUNT; i++) {
            cmd.goRight(SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
            resetFlyCount = i + 1;

            if (targetFound(getCurrentFlightController())) return;
        }

        for (int i = 0; i < resetFlyCount; i++) {
            cmd.goLeft(SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
        }
    }

    private boolean targetFound(FlightController flightController) {
        boolean found = qrController.getLastScan() != null && flightController.getFlightDirection() != Direction.UNKNOWN;

        System.out.println(ConsoleColors.CYAN_BRIGHT + "Found: " + found + ConsoleColors.RESET);

        return found;
    }

//    private void searchForUnknownQrLocation() {
//        Direction lastKnownQrLocation;
//        boolean qrWasFound = false;
//
//        for (int i = 0; i < 3; i++) {
//            lastKnownQrLocation = qrController.getLastKnownQrDirection();
//
//            switch (lastKnownQrLocation) {
//                case LEFT:
//                    cmd.goRight(speed / 2).doFor(500); break;
//                case RIGHT:
//                    cmd.goLeft(speed / 2).doFor(500); break;
//                case CENTER:
//                    cmd.backward(speed / 2).doFor(500); break;
//            }
//
//            if (qrController.getLastScan() != null && qrController.getQrDirection() != Direction.UNKNOWN) break;
//
////            if (lastKnownQrLocation == Direction.CENTER) break; // don't break, we need to fly randomly to the right and to the left
//        }
//public void searchForQr() {
//    String qrString = null;
//
//    goToDetectionAltitude();
//    qrController.resetLastScan();
//
//    for (int i = 0; i < 5; i++) {
//        System.out.println("Iteration: " + i);
//
//        // FORWARD
//        cmd.forward(speed).doFor(1000);
//        cmd.hover().doFor(1000);
//        qrString = qrController.getLastScan();
//        if (qrString != null) break;
//
//        // SPIN LEFT
//        cmd.spinLeft(100).doFor(100);
//        cmd.hover().doFor(1000);
//        qrString = qrController.getLastScan();
//        if (qrString != null) {
//            cmd.goRight(100).doFor(100);
//            break;
//        }
//
//        // SPIN RIGHT
//        cmd.spinRight(100).doFor(100);
//        cmd.spinRight(100).doFor(100);
//        cmd.hover().doFor(1000);
//        qrString = qrController.getLastScan();
//        // SPIN BACK (reset)
//        cmd.spinLeft(100).doFor(100);
//        if (qrString != null) break;
//
//        // PAN LEFT
//        cmd.goLeft(speed).doFor(500);
//        cmd.hover().doFor(1000);
//        qrString = qrController.getLastScan();
//        if (qrString != null) {
//            cmd.goRight(speed).doFor(250);
//            break;
//        }
//
//        // PAN RIGHT
//        cmd.goRight(speed).doFor(500);
//        cmd.hover().doFor(500);
//        cmd.goRight(speed).doFor(500);
//        cmd.hover().doFor(1000);
//        qrString = qrController.getLastScan();
//        // PAN BACK (reset)
//        cmd.goLeft(speed).doFor(500);
//        if (qrString != null) break;
//
//        cmd.hover().doFor(1000);
//    }
//
//    qrController.resetLastScan();
//    System.out.println(ConsoleColors.CYAN_BOLD_BRIGHT + "I FOUND QR: " + qrString + ConsoleColors.RESET);
//    cmd.landing();
//
//    goToRingAltitude();
//}

//    }


    public void LEDSuccess() {
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 1);
    }

    public static int getSpeed() {
        return speed;
    }

    public static void setSpeed(int speed) {
        DroneController.speed = speed;
    }

    public int getDroneAltitude() {
        return droneAltitude;
    }

    public int getDroneBattery() {
        return droneBattery;
    }

    public IARDrone getDrone() {
        return drone;
    }

    public FlightController getCurrentFlightController() {
        return flightControllers.get(currentFlightController);
    }

    public String getCurrentFlightControllerName() {
        final String[] classNameList = getCurrentFlightController().getClass().getName().split("\\.");
        return classNameList[classNameList.length - 1];
    }

    public void nextFlightController() {
        if (currentFlightController < flightControllers.size() - 1) {
            currentFlightController += 1;
        } else {
            currentFlightController = 0;
        }

        System.out.println(ConsoleColors.CYAN_BRIGHT + "Changed the flight controller." + ConsoleColors.RESET);
        writeFlightController();
    }

    private void writeFlightController() {
        System.out.println(ConsoleColors.CYAN_BRIGHT +
                "Current flight controller is " +
                ConsoleColors.YELLOW_BRIGHT + getCurrentFlightControllerName() + ConsoleColors.CYAN_BRIGHT +
                " (" +
                ConsoleColors.YELLOW_BRIGHT + currentFlightController + ConsoleColors.CYAN_BRIGHT +
                ". position)." + ConsoleColors.RESET);
    }
}
