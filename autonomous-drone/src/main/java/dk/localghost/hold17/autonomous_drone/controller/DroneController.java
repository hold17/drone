package dk.localghost.hold17.autonomous_drone.controller;

import dk.localghost.hold17.autonomous_drone.opencv_processing.CircleFilter;
import dk.localghost.hold17.autonomous_drone.opencv_processing.RectangleFilter;
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
    private static RectangleFilter rectangleFilter = new RectangleFilter();

    private final static int MAX_ALTITUDE = 1400;
    private final static int MIN_ALTITUDE = 975;
    private int droneAltitude = 0;
    private int droneBattery = 0;
    private boolean droneFlying = false;
    private int speed;

    public DroneController(IARDrone drone, int speed) {
        this.drone = drone;
        this.cmd = this.drone.getCommandManager();
        this.speed = speed;
        initializeDrone();
    }

    private void initializeDrone() {
        drone.start();
        drone.reset();

        drone.getCommandManager().setMaxVideoBitrate(4000);
        drone.getCommandManager().setVideoBitrate(4000);

        drone.setMinAltitude(MIN_ALTITUDE); // TODO Doesn't work..
        drone.setMaxAltitude(2000);
        initializeListeners();

        System.out.println(ConsoleColors.BLUE_BRIGHT + "CURRENT BATTERY: " + droneBattery + "%" + ConsoleColors.RESET);

        if (droneBattery < 20) {
            System.out.println(ConsoleColors.YELLOW_BOLD_BRIGHT + "WARNING: Battery percentage low (" + droneBattery + "%)!" + ConsoleColors.RESET);
        }

        qrController = new QRScannerController();
        qrScanner = new QRCodeScanner();
        qrScanner.addListener(qrController);

        flightControllers.add(qrController);
        flightControllers.add(rectangleFilter);

        writeFlightController();

        LEDSuccess();
    }

    public void updateQR(BufferedImage bufferedImage) {
        qrScanner.lookForQRCode(bufferedImage);
    }

    public RectangleFilter getRectangleFilter() {
        return rectangleFilter;
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
            public void receivedExtendedAltitude(Altitude d) {}
        });

        drone.getNavDataManager().addBatteryListener(new BatteryListener() {
            @Override
            public void batteryLevelChanged(int percentage) {
                droneBattery = percentage;
            }

            @Override
            public void voltageChanged(int vbat_raw) {}
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
        System.out.println("GOING THROUGH TARGET!");
        // Change this value to change the distance to fly when flying through rings
        int forwardTime = 1000;
//        double distance = getCurrentFlightController().distanceFromTarget();

//        if (distance > 60 && distance < 80) {
//            forwardTime = 1500;
//        }

        // UP
        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 1);
        goToRingAltitude();

        // ALIGN
        Direction direction = getFlightController(FlightControllerType.OpenCV_QR).getFlightDirection();
        while (direction != Direction.CENTER) {
            switch (direction) {
                case LEFT:
                    cmd.goLeft(speed).doFor(100); break;
                case RIGHT:
                    cmd.goRight(speed).doFor(100); break;
            }
            cmd.hover().waitFor(150);
        }

        // FORWARD
        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 1);
        cmd.forward(100).doFor(forwardTime);

        cmd.hover().doFor(3000);

        // DOWN
        goToDetectionAltitude();
        cmd.hover().doFor(250);
        System.out.println("WENT THROUGH TARGET!");
    }

    public void alignYawCC() {
        setFlightController(FlightControllerType.OpenCV_QR); // OpenCV QR Detection

        Direction flightDirection = getCurrentFlightController().getFlightDirection();
        while(flightDirection == Direction.LEFT || flightDirection == Direction.UNKNOWN) {
            System.out.println("Flight direction: " + flightDirection);
            flightDirection = getCurrentFlightController().getFlightDirection();

            cmd.spinLeft(15).doFor(100);
            cmd.hover().waitFor(250);
        }

        cmd.hover().waitFor(1500);

        while (flightDirection == Direction.RIGHT) {
            System.out.println("Flight direction: " + flightDirection);
            flightDirection = getCurrentFlightController().getFlightDirection();

            cmd.spinRight(15).doFor(100);
            cmd.hover().waitFor(250);
        }

        setFlightController(FlightControllerType.ZX_QR);
    }

    public void alignYawC() {
        setFlightController(FlightControllerType.OpenCV_QR); // OpenCV QR Detection

        Direction flightDirection = getCurrentFlightController().getFlightDirection();
        while(flightDirection == Direction.RIGHT || flightDirection == Direction.UNKNOWN) {
            System.out.println("Flight direction: " + flightDirection);
            flightDirection = getCurrentFlightController().getFlightDirection();

            cmd.spinRight(15).doFor(100);
            cmd.hover().waitFor(250);
        }

        setFlightController(FlightControllerType.ZX_QR);
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
        System.out.println(ConsoleColors.YELLOW_BRIGHT + "Going to altitude: " + altitude + ConsoleColors.RESET);
        cmd.hover().waitFor(2000);
        if (droneAltitude < altitude) {
            while(droneAltitude < altitude) {
                cmd.up(50).doFor(30);
            }
        } else {
            while(droneAltitude > altitude) {
                cmd.down(50).doFor(30);
            }
            while(droneAltitude < altitude) {
                cmd.up(50).doFor(30);
            }
        }
    }

    public void rotateYaw_p00() {
        cmd.setLedsAnimation(LEDAnimation.RED_SNAKE, 6, 5);
        cmd.takeOff().doFor(6000);
        goToDetectionAltitude();
        cmd.forward(20).doFor(800);
//        cmd.goLeft(20).doFor(500);
        cmd.hover().waitFor(500);
//        cmd.spinLeft(40).doFor(750);
        alignYawCC();
        cmd.hover().waitFor(250);
        cmd.forward(speed).doFor(800).hover();
        alignTarget();

        System.out.println(ConsoleColors.GREEN_BRIGHT + "alignTarget() just finished." + ConsoleColors.RESET);
        cmd.setLedsAnimation(LEDAnimation.SNAKE_GREEN_RED, 5, 2);

        cmd.hover().waitFor(2500);
        rotateYaw_p01();
    }

    public void rotateYaw_p01() {
//        cmd.spinRight(40).doFor(950);
        alignYawC();
        cmd.hover().waitFor(250);
        alignTarget();

        cmd.setLedsAnimation(LEDAnimation.SNAKE_GREEN_RED, 5, 2);

        cmd.hover().waitFor(2500);
        rotateYaw_p02();
    }

    public void rotateYaw_p02() {
//        cmd.spinRight(40).doFor(900);
        alignYawC();
        cmd.hover().waitFor(250);
        alignTarget();

        cmd.setLedsAnimation(LEDAnimation.SNAKE_GREEN_RED, 5, 2);

        cmd.hover().waitFor(500);
        cmd.landing();
    }

    private Direction getTargetDirection() {
        setFlightController(FlightControllerType.ZX_QR);

        Direction currentDirection = getCurrentFlightController().getFlightDirection();

        if (currentDirection == Direction.UNKNOWN) {
            setFlightController(FlightControllerType.OpenCV_QR);
            Direction tempDirection = getCurrentFlightController().getFlightDirection();

            if (tempDirection != Direction.UNKNOWN) {
                currentDirection = tempDirection;
            }
        }

        setFlightController(FlightControllerType.ZX_QR);

        return currentDirection;
    }

    public void alignTarget() {
        Direction targetDirection;
//        boolean secondUnknown = false;

//        goToDetectionAltitude();
//        goToRingAltitude();

        for (int i = 0; i < 25; i++) {
            System.out.println(ConsoleColors.CYAN_BRIGHT + "==== Iteration " + i + " ====" + ConsoleColors.RESET);
            targetDirection = getTargetDirection();

//            if (getCurrentFlightController().readyForFlyingThroughRing()) break;
             if (getFlightController(FlightControllerType.ZX_QR).readyForFlyingThroughRing()) {
                 System.out.println(ConsoleColors.YELLOW_BRIGHT + "Ready to fly through the ring!!!" + ConsoleColors.RESET);
                 flyThroughRing();
                 cmd.hover();
//                 cmd.landing();
                 return;
             }

            switch (targetDirection) {
                case LEFT:
                    cmd.goLeft(speed).doFor(650);
//                    cmd.spinRight(20).doFor(100);
//                    goForward();
                    break;
                case RIGHT:
                    cmd.goRight(speed).doFor(650);
//                    cmd.spinRight(20).doFor(100);
//                    goForward();
                    break;
                case CENTER:
                    cmd.forward(speed).doFor(800);
//                    goForward();
                    break;
                case UNKNOWN:
                    flyToLastKnownDirection();
                    if (targetFound(getCurrentFlightController())) break;
//                    if (!secondUnknown) {
//                        secondUnknown = true;
//                        nextFlightController();
//                        continue;
//                    } else {
//                        secondUnknown = false;
//                        setFlightController(FlightControllerType.ZX_QR);
//                    }
                    cmd.hover().waitFor(3000);
                    if (targetFound(getCurrentFlightController())) break;
                    searchForLostTarget(2);
                    qrController.resetLastScan();
                    break;
            }

            getCurrentFlightController().resetFlightDirection();
            cmd.hover().waitFor(2000);

            if (getFlightController(FlightControllerType.ZX_QR).readyForFlyingThroughRing()) break;
        }

        if (getCurrentFlightController().readyForFlyingThroughRing()) {
            System.out.println(ConsoleColors.YELLOW_BRIGHT + "Ready to fly through the ring!!! 2nd time" + ConsoleColors.RESET);
            flyThroughRing();
            cmd.hover();
        }
//        System.out.println(ConsoleColors.RED_BRIGHT + "I failed, sorry my lort :(" + ConsoleColors.RESET);
//        cmd.landing();

    }

    private void flyToLastKnownDirection() {
        switch (getCurrentFlightController().getLastKnownDirection()) {
            case LEFT:
                cmd.goLeft(speed).doFor(550);
                break;
            case RIGHT:
                cmd.goRight(speed).doFor(550);
                break;
            case CENTER:
                cmd.forward(speed).doFor(650);
                break;
        }
    }

    /**
     * Makes minor adjustments to find the target that was recently lost
     *
     * @param searchCount  how many iterations the search algorithm should execute
     */
    private void searchForLostTarget(int searchCount) {
        final int FLY_SPEED = (speed / 2) - 20;
        final int FLY_TIME = 450;
        final int WAIT_TIME = 2000;
        final int TEST_COUNT = 1;

        for (FlightController fc : flightControllers) {
            for (int i = 0; i < searchCount; i++) {
                cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 1);
                testRight(FLY_SPEED, FLY_TIME, WAIT_TIME, TEST_COUNT);
                if (targetFound(fc)) return;

                cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 1);
                testLeft(FLY_SPEED, FLY_TIME, WAIT_TIME, TEST_COUNT);
                if (targetFound(fc)) return;



                cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 1);
                cmd.forward(FLY_SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
                if (targetFound(fc)) return;
            }
        }

        takeoffOrLand();
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

    private void testRight(final int SPEED, int FLY_TIME, final int WAIT_TIME, final int COUNT) {
        FLY_TIME += 100;
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


    public void LEDSuccess() {
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 1);
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
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

    public FlightController getFlightController(FlightControllerType fcType) {
        return flightControllers.get(fcType.getValue());
    }

    public FlightController getCurrentFlightController() {
        return flightControllers.get(currentFlightController);
    }

    public String getCurrentFlightControllerName() {
        final String[] classNameList = getCurrentFlightController().getClass().getName().split("\\.");
        return classNameList[classNameList.length - 1];
    }


    public void setFlightController(FlightControllerType fcType) {
        if (currentFlightController == fcType.getValue()) return;

        currentFlightController = fcType.getValue();

        System.out.println(ConsoleColors.CYAN_BRIGHT + "Changed the flight controller." + ConsoleColors.RESET);
        writeFlightController();
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
