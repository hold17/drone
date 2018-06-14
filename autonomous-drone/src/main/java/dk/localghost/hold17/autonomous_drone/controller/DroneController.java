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

public class DroneController {
    private IARDrone drone;
    private CommandManager cmd;
    private QRCodeScanner qrScanner;
    private QRScannerController qrController;
    private FlightController flightController;

    private static CircleFilter circleFilter = new CircleFilter();
    private static RectangleFilter rectangleFilter = new RectangleFilter();

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
        drone.getCommandManager().flatTrim();

        drone.setMinAltitude(MIN_ALTITUDE); // TODO Doesn't work..
        drone.setMaxAltitude(MAX_ALTITUDE);
        initializeListeners();

        System.out.println(ConsoleColors.BLUE_BRIGHT + "CURRENT BATTERY: " + droneBattery + "%" + ConsoleColors.RESET);

        if (droneBattery < 20) {
            System.out.println(ConsoleColors.YELLOW_BOLD_BRIGHT + "WARNING: Battery percentage low (" + droneBattery + "%)!" + ConsoleColors.RESET);
        }
        //TODO Check if this should be commented in
//        qrScanner.addListener(qrController);
        qrController = new QRScannerController();
        qrScanner = new QRCodeScanner();

        flightController = qrController;

        LEDSuccess();
    }

    public void updateQR(BufferedImage bufferedImage) {
        qrScanner.lookForQRCode(bufferedImage);
    }

    public CircleFilter getCircleFilter() {
        return circleFilter;
    }
    public RectangleFilter getRectangleFilter() { return rectangleFilter;  }


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
            droneFlying = true;
        } else {
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
        cmd.hover();
    }

    /**
     * Interrupts and resets the drone. Emergency land
     */
    public void reset() {
        cmd.stop();
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
        cmd.hover().doFor(250);
        goToMinimumAltitude();
        cmd.hover().doFor(250);
        // Change this value to change the distance to fly when flying through rings
        final int FORWARD_TIME = 1500;

        // UP
        System.out.println("          FLYING UP");
        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 1);
        goToMaxmimumAltitude();

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
        goToMinimumAltitude();

        // WAIT
        cmd.hover().doFor(250);
    }

    public void goToMinimumAltitude() {
        if (droneAltitude > MIN_ALTITUDE) {
            while(droneAltitude > MIN_ALTITUDE) {
                cmd.down(speed).doFor(250);
            }
        } else {
            while(droneAltitude < MIN_ALTITUDE) {
                cmd.up(speed).doFor(250);
            }
        }
    }

    public void goToMaxmimumAltitude() {
        if (droneAltitude < MAX_ALTITUDE) {
            while(droneAltitude < MAX_ALTITUDE) {
                cmd.up(speed).doFor(250);
            }
        } else {
            while(droneAltitude > MAX_ALTITUDE) {
                cmd.down(speed).doFor(250);
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



    public void alignCircle() {
        Direction directionToCircleCenter = null;
//        Remove this line of code if testing on table.
//        goToMaxmimumAltitude();
        System.out.println("IM AT THE TOP");
        while (directionToCircleCenter != Direction.CENTER) {
            Direction tempDirection = Direction.findXDirection(circleFilter.getBiggestCircle().x); // henter enum ud fra fundne stoerste cirkel
            if (tempDirection != Direction.UNKNOWN) {
                directionToCircleCenter = tempDirection;
            }

            System.out.println(ConsoleColors.WHITE_UNDERLINED + ConsoleColors.GREEN + "CIRCLE IS TO THE " + directionToCircleCenter + ConsoleColors.RESET);
            if (directionToCircleCenter != null) {
                switch (directionToCircleCenter) {
                    case LEFT:
                        cmd.setLedsAnimation(LEDAnimation.BLINK_RED, 6, 1);
                        cmd.goLeft(speed).doFor(500);
                        break;
                    case RIGHT:
                        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 6, 1);
                        cmd.goRight(speed).doFor(500);
                        break;
                    case CENTER:
                        LEDSuccess();
                        cmd.forward(speed).doFor(500);
                        System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + "Found circle" + ConsoleColors.RESET);
                        break;
                }
            }
            cmd.hover();
            cmd.waitFor(1000);
        }

        cmd.setLedsAnimation(LEDAnimation.SNAKE_GREEN_RED, 1, 10);
//        drone.landing();
    }
    //        return rectangleFilter.findPaperPosition(rectangleFilter.getBiggestQRCode());
    //        rectangleFilter.findBiggestQRCode(rectangleFilter.filterImage(droneCamera));
    //
    //        RectangleFilter rectangleFilter = new RectangleFilter();
//    private Direction getPaperDirection() {

//    }

    public void alignQrCode() {
        Direction qrDirection = Direction.UNKNOWN;

        for (int i = 0; i < 10; i++) {
             qrDirection = flightController.getFlightDirection();

            switch (qrDirection) {
                case LEFT:
                    cmd.goRight(speed).doFor(500);
                    break;
                case RIGHT:
                    cmd.goLeft(speed).doFor(500);
                    break;
                case CENTER:
                    cmd.forward(speed).doFor(500);
                    break;
                case UNKNOWN:
                    searchForUnknownQrLocation();
                    qrController.resetLastScan();
                    break;

            }

            cmd.hover().waitFor(1000);

            flightController.resetFlightDirection();
        }
    }

    /**
     * Makes minor adjustments to find a qr code that was recently lost
     */
    private void searchForLostQr(int searchCount) {
        boolean qrWasFound = false;

        final int FLY_SPEED = speed / 2;
        final int FLY_TIME = 500;
        final int WAIT_TIME = 250;
        final int TEST_COUNT = 2;

        for (int i = 0; i < searchCount; i++) {
            testLeft(FLY_SPEED, FLY_TIME, WAIT_TIME, TEST_COUNT);
            if (lostQrWasFound()) return;
            testRight(FLY_SPEED, FLY_TIME, WAIT_TIME, TEST_COUNT);
            if (lostQrWasFound()) return;


            cmd.backward(FLY_SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
            if (lostQrWasFound()) return;
        }
    }

    private void testLeft(final int SPEED, final int FLY_TIME, final int WAIT_TIME, final int COUNT) {
        int resetFlyCount = 0;

        for (int i = 0; i < COUNT; i++) {
            cmd.goLeft(SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
            resetFlyCount = i + 1;

            if (lostQrWasFound()) break;
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

            if (lostQrWasFound()) break;
        }

        for (int i = 0; i < resetFlyCount; i++) {
            cmd.goLeft(SPEED).doFor(FLY_TIME).hover().waitFor(WAIT_TIME);
        }
    }

    private boolean lostQrWasFound() {
        return qrController.getLastScan() != null && flightController.getFlightDirection() != Direction.UNKNOWN;
    }

    private void searchForUnknownQrLocation() {
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
    }


    public void searchForQr() {
        String qrString = null;

        goToMinimumAltitude();
        qrController.resetLastScan();

        for (int i = 0; i < 5; i++) {
            System.out.println("Iteration: " + i);

            // FORWARD
            cmd.forward(speed).doFor(1000);
            cmd.hover().doFor(1000);
            qrString = qrController.getLastScan();
            if (qrString != null) break;

            // SPIN LEFT
            cmd.spinLeft(100).doFor(100);
            cmd.hover().doFor(1000);
            qrString = qrController.getLastScan();
            if (qrString != null) {
                cmd.goRight(100).doFor(100);
                break;
            }

            // SPIN RIGHT
            cmd.spinRight(100).doFor(100);
            cmd.spinRight(100).doFor(100);
            cmd.hover().doFor(1000);
            qrString = qrController.getLastScan();
            // SPIN BACK (reset)
            cmd.spinLeft(100).doFor(100);
            if (qrString != null) break;

            // PAN LEFT
            cmd.goLeft(speed).doFor(500);
            cmd.hover().doFor(1000);
            qrString = qrController.getLastScan();
            if (qrString != null) {
                cmd.goRight(speed).doFor(250);
                break;
            }

            // PAN RIGHT
            cmd.goRight(speed).doFor(500);
            cmd.hover().doFor(500);
            cmd.goRight(speed).doFor(500);
            cmd.hover().doFor(1000);
            qrString = qrController.getLastScan();
            // PAN BACK (reset)
            cmd.goLeft(speed).doFor(500);
            if (qrString != null) break;

            cmd.hover().doFor(1000);
        }

        qrController.resetLastScan();
        System.out.println(ConsoleColors.CYAN_BOLD_BRIGHT + "I FOUND QR: " + qrString + ConsoleColors.RESET);
        cmd.landing();

        goToMaxmimumAltitude();
    }

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

    // funk til at centrere ud for cirklen

    public void centerRing(){
        int counter = 0;

        Direction xDirection;
        goToMaxmimumAltitude();
        while (counter <= 200)
        {
//            goToMaxmimumAltitude();
            //
            xDirection = Direction.findXDirection(circleFilter.getAverageCenter().x); // henter enum ud fra fundne stoerste cirkel
            //direction = direction;
            if ( xDirection == Direction.UNKNOWN)
            {
                System.out.println("Fandt ikke retningen");
            } else{
                switch (xDirection)
                {
                    case LEFT:
                        // TODO: jeg kender ikke til syntaksen for at bevæge dronen
                        cmd.goLeft(speed).doFor(300);
                        System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT+"GOING LEFT...." + ConsoleColors.RESET);
                        break;
                    case RIGHT:
                        cmd.goRight(speed).doFor(300);
                        System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT+"GOING RIGHT..." + ConsoleColors.RESET);
                        break;
                    case CENTER:
                        if (doFlyForward()){
                            cmd.forward(speed).doFor(1250);
                        } else{
                            cmd.forward(speed).doFor(300);
                        }
                        System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT +"VI FANDT CENTER!!!!"+ ConsoleColors.RESET);
                        break;
                }
                circleFilter.clearAverageArray();
            }

            counter++;
            System.out.println("Sover 0.5 sekund...");
            cmd.hover();
            cmd.waitFor(500);
        }
        System.out.println("Counter: " + counter);
        System.out.println("switch done");
    }


    public boolean doFlyForward(){
        int maxRadius = 350; // værdi der angiver hvornår vi skal flyve igennem cirklen
        double radius;
        radius = circleFilter.getMaxRadius();

        if (radius >= maxRadius) return true;
        else{ return false; }
    }


}
