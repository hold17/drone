package dk.localghost.hold17.autonomous_drone;

import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.CommandManager;
import dk.localghost.hold17.base.command.LEDAnimation;
import dk.localghost.hold17.base.navdata.BatteryListener;

public class CircleChase {
    public static final int SPEED = 50;
    private IARDrone drone = null;
    private boolean inAir;

    private int currentBatteryPercentage;


    public CircleChase(IARDrone drone) {
        this.drone = drone;
        this.inAir = false;
        drone.getCommandManager().flatTrim();
        drone.getNavDataManager().addBatteryListener(new BatteryListener() {
            @Override
            public void batteryLevelChanged(int percentage) {
                if (currentBatteryPercentage != percentage) {
                    currentBatteryPercentage = percentage;
                    System.out.println("         BATTERY: " + percentage + "%");
                }
            }

            @Override
            public void voltageChanged(int vbat_raw) {

            }
        });
    }

    public void animateLeds() {
        drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN_RED, 3, 10);
    }

    public void emergencyStop() {
        drone.getCommandManager().emergency();
        this.inAir = false;
    }

    public void dance() {
        final CommandManager cmd = drone.getCommandManager();
        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 5);
        cmd.takeOff().hover().waitFor(5000);
        System.out.println("!!!!!takeoff completed!!!!!");


        cmd.setLedsAnimation(LEDAnimation.BLINK_RED, 10, 5);
        System.out.println("TURNING LEFT");
        cmd.spinLeft(SPEED).doFor(5000);

        cmd.waitFor(5000);

//        cmd.setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);
//        System.out.println("TURNING RIGHT");
//        cmd.spinRight(SPEED).doFor(5000);

        cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 5);
        System.out.println("HOVERING");
        cmd.landing();
    }

    public void clockwise() {
        drone.getCommandManager().spinLeft(SPEED).waitFor(1000);
        drone.getCommandManager().freeze().hover();
    }

    public void counterClockwise() {
        drone.getCommandManager().spinRight(SPEED).waitFor(1000);
        drone.getCommandManager().freeze().hover();
    }

    public void left() {
        drone.getCommandManager().goLeft(SPEED).waitFor(1000);
        drone.getCommandManager().freeze().hover();
    }

    public void right() {
        drone.getCommandManager().goRight(SPEED).waitFor(1000);
        drone.getCommandManager().freeze().hover();
    }

    public void forward() {
        drone.getCommandManager().forward(SPEED).waitFor(1000);
        drone.getCommandManager().freeze().hover();
    }

    public void backward() {
        drone.getCommandManager().backward(SPEED).waitFor(1000);
        drone.getCommandManager().freeze().hover();
    }

    public void takeoffLand() {
        if (!inAir) {
            drone.getCommandManager().takeOff();
            drone.hover();
            this.inAir = true;
        } else {
            drone.freeze();
            drone.getCommandManager().landing();
            this.inAir = false;
        }
    }
}
