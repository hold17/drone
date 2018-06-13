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

public class DroneController {
    private IARDrone drone;
    private CommandManager cmd;
    QRCodeScanner qrScanner = new QRCodeScanner();
    QRScannerController qrController = new QRScannerController();

    private final static int MAX_ALTITUDE = 1400;
    private final static int MIN_ALTITUDE = 900;
    private int droneAltitude = 0;
    private int droneBattery = 0;
    private boolean droneFlying = false;

//    private BufferedImage droneCamera;

    private static int speed;
    private static CircleFilter circleFilter;
    public static int cameraWidth = 1280;
    public static int cameraHeight = 720;

    public DroneController(IARDrone drone, int speed) {
        this.drone = drone;
        this.cmd = this.drone.getCommandManager();
        this.speed = speed;
        circleFilter = new CircleFilter();
        qrScanner.addListener(qrController);
        initializeDrone();
    }

    private void initializeDrone() {
        drone.start();
        drone.getCommandManager().flatTrim();

        drone.setMinAltitude(MIN_ALTITUDE); // TODO Doesn't work..
        drone.setMaxAltitude(4000);
        initializeListeners();

        System.out.println(ConsoleColors.BLUE_BRIGHT + "CURRENT BATTERY: " + droneBattery + "%" + ConsoleColors.RESET);

        if (droneBattery < 20) {
            System.out.println(ConsoleColors.YELLOW_BOLD_BRIGHT + "WARNING: Battery percentage low (" + droneBattery + "%)!" + ConsoleColors.RESET);
        }

//        drone.getVideoManager().addImageListener(camera -> this.droneCamera = camera);

//        drone.getVideoManager().addImageListener(image -> {
//            circleFilter.findCircleAndDraw(image);
//            Direction.CAMERA_WIDTH = droneCamera.getWidth();
//            alignCircle();
//        });

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
        System.out.println("IM AT TOP");
        int count = 0;
        while (directionToCircleCenter != Direction.CENTER) {
            Direction tempDirection = Direction.findXDirection(circleFilter.getBiggestCircle().x); // henter enum ud fra fundne stoerste cirkel
            if (tempDirection != Direction.UNKNOWN) {
                directionToCircleCenter = tempDirection;
            }

//            System.err.print("*** ");
            System.out.println(ConsoleColors.WHITE_UNDERLINED + ConsoleColors.GREEN + "CIRCLE IS TO THE " + directionToCircleCenter + ConsoleColors.RESET);
//            System.err.print(" ***");
            if (directionToCircleCenter != null) {
                switch (directionToCircleCenter) {
                    case LEFT:
                    case LEFTDOWN:
                    case LEFTUP:
                        cmd.setLedsAnimation(LEDAnimation.BLINK_RED, 6, 1);
                        cmd.goLeft(speed).doFor(500);
                        break;
                    case RIGHT:
                    case RIGHTUP:
                    case RIGHTDOWN:
                        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 6, 1);
                        cmd.goRight(speed).doFor(500);
                        break;
                    case DOWN:
                    case UP:
                    case CENTER:
                        LEDSuccess();
                        cmd.forward(speed).doFor(500);
                        System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + "Found circle" + ConsoleColors.RESET);
                        break;
                }
            }
            count++; // TODO: TBD if this if can stay in while loop until.
            cmd.hover();
            cmd.waitFor(1000);
        }

        cmd.setLedsAnimation(LEDAnimation.SNAKE_GREEN_RED, 1, 10);
//        drone.landing();
    }

//    private Direction getPaperDirection() {
//        RectangleFilter rectangleFilter = new RectangleFilter();
//
//        rectangleFilter.findBiggestQRCode(rectangleFilter.filterImage(droneCamera));
//        return rectangleFilter.findPaperPosition(rectangleFilter.getBiggestQRCode());
//    }

//    public void alignQrCode() {
//        final Direction qrDirection = qrController.getQrDirection();
//
//        for (int i = 0; i < 10; i++) {
//            if (qrDirection == Direction.LEFT) {
//                cmd.goLeft(speed).doFor(500);
//            } else if (qrDirection == Direction.RIGHT) {
//                cmd.goRight(speed).doFor(500);
//            } else if (qrDirection == Direction.CENTER) {
//                cmd.setLedsAnimation(LEDAnimation.BLINK_RED, 10, 2);
//            } else if (qrDirection == Direction.UNKNOWN) {
//                System.out.println("UNKNOWN");
//            }
//
//            cmd.hover();
//            cmd.waitFor(1000);
//
//            qrController.resetQrDirection();
//        }


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

}
