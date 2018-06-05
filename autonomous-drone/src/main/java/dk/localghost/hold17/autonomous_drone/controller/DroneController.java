package dk.localghost.hold17.autonomous_drone.controller;

import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.CommandManager;
import dk.localghost.hold17.base.command.LEDAnimation;
import dk.localghost.hold17.base.navdata.Altitude;
import dk.localghost.hold17.base.navdata.AltitudeListener;
import dk.localghost.hold17.base.navdata.BatteryListener;

public class DroneController {
    private IARDrone drone;
    private CommandManager cmd;

    private final static int MAX_ALTITUDE = 2000;
    private final static int MIN_ALTITUDE = 1000;

    private int droneAltitude = 0;
    private int droneBattery = 0;
    private boolean droneFlying = false;

    private static int speed;

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
        // Change this value to change the distance to fly when flying through rings
        final int FORWARD_TIME = 1000;

        // UP
        System.out.println("          FLYING UP");
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 1);
        while(droneAltitude < MAX_ALTITUDE) {
            drone.getCommandManager().up(100).doFor(500);
        }

        // WAIT
        drone.getCommandManager().hover().doFor(250);

        // FORWARD
        System.out.println("          FLYING FORWARD");
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 1);
        drone.getCommandManager().forward(speed).doFor(FORWARD_TIME);

        // WAIT
        drone.getCommandManager().hover().doFor(250);

        // DOWN
        System.out.println("          FLYING DOWN");
        while(droneAltitude > MIN_ALTITUDE) {
            drone.getCommandManager().down(speed).doFor(500);
        }

        // WAIT
        drone.getCommandManager().hover().doFor(250);
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
