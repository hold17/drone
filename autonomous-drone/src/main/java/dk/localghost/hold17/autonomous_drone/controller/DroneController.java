package dk.localghost.hold17.autonomous_drone.controller;

import dk.localghost.hold17.autonomous_drone.opencv_processing.Direction;
import dk.localghost.hold17.autonomous_drone.opencv_processing.ImageProcessor;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.CommandManager;
import dk.localghost.hold17.base.command.LEDAnimation;
import dk.localghost.hold17.base.navdata.Altitude;
import dk.localghost.hold17.base.navdata.AltitudeListener;
import dk.localghost.hold17.base.navdata.BatteryListener;

import java.awt.image.BufferedImage;

public class DroneController {
    private IARDrone drone;
    private CommandManager cmd;
    private QRScannerController qrController;

    private final static int MAX_ALTITUDE = 1400;
    private final static int MIN_ALTITUDE = 900;

    private int droneAltitude = 0;
    private int droneBattery = 0;
    private boolean droneFlying = false;

    private BufferedImage droneCamera;

    private static int speed;

    public static int cameraWidth;
    public static int cameraHeight;

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

        System.out.println("CURRENT BATTERY: " + droneBattery + "%");

        if (droneBattery < 20) {
            System.out.println("WARNING: Battery percentage low (" + droneBattery + "%)!");
        }

        drone.getVideoManager().addImageListener(camera -> this.droneCamera = camera);

        final QRCodeScanner qrScanner = new QRCodeScanner();
        qrController = new QRScannerController();
        drone.getVideoManager().addImageListener(qrScanner::imageUpdated);
        qrScanner.addListener(qrController);

        LEDSuccess();
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

    public void bum() {
        final Direction paperDirection = getPaperDirection();

        switch (paperDirection) {
            case LEFT:
                System.out.println("Left"); break;
            case RIGHT:
                System.out.println("Right"); break;
            case CENTER:
                System.out.println("Center"); break;
            case UNKNOWN:
                System.out.println("Unknown"); break;
        }
    }

    private Direction getPaperDirection() {
        ImageProcessor imageProcessor = new ImageProcessor();

        imageProcessor.findBiggestQRCode(imageProcessor.filterImage(droneCamera));
        return imageProcessor.findPaperPosition(imageProcessor.getBiggestQRCode());
    }

    public void alignQrCode() {
        final Direction qrDirection = qrController.getQrDirection();

        for (int i = 0; i < 10; i++) {
            if (qrDirection == Direction.LEFT) {
                cmd.goLeft(speed).doFor(500);
            } else if (qrDirection == Direction.RIGHT) {
                cmd.goRight(speed).doFor(500);
            } else if (qrDirection == Direction.CENTER) {
                cmd.setLedsAnimation(LEDAnimation.BLINK_RED, 10, 2);
            } else if (qrDirection == Direction.UNKNOWN) {
                System.out.println("UNKNOWN");
            }

            cmd.hover();
            cmd.waitFor(1000);

//            qrController.resetQrDirection();
        }
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
