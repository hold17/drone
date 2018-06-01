package dk.localghost.hold17.autonomous_drone;

import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;

import java.io.IOException;
import java.util.Scanner;

public class StuffAndThings {

    public static void main(String[] args) throws IOException {
        final IARDrone drone = new ARDrone("10.0.1.2");

        drone.start();

        CircleChase cc = new CircleChase(drone);

        System.out.println("Done");

        final Scanner scn = new Scanner(System.in);

        while(true) {
            final String str = scn.nextLine().toLowerCase();

            if (str.equals("q")) {
//                drone.stop();
                drone.disconnect();
                break;
            } else if (str.equals("")) {
                cc.takeoffLand();
            } else if (str.equals("animate")) {
                cc.animateLeds();
            } else if (str.equals("dance")) {
                cc.dance();
            } else if (str.equals("takeoff")) {
                drone.takeOff();
            } else if (str.equals("land")) {
                drone.freeze();
                drone.landing();
            } else if (str.equals("c")) cc.clockwise();
            else if (str.equals("cc")) cc.counterClockwise();
            else if (str.equals("forward")) cc.forward();
            else if (str.equals("backward")) cc.backward();
            else if (str.equals("left")) cc.left();
            else if (str.equals("right")) cc.right();
            else if (str.equals("cal")) drone.getCommandManager().flatTrim();
            else if (str.equals("h")) drone.getCommandManager().hover();
            else if (str.equals("reset")) drone.reset();
            else if (str.equals("restart")) drone.restart();
        }

    }
}
