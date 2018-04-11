package dk.localghost.hold17.apps.tutorial;


import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.exception.ARDroneException;
import dk.localghost.hold17.base.exception.IExceptionListener;

public class TutorialMain {

    public static void main(String[] args) {
        IARDrone drone = null;
        try {
            // Tutorial Section 1
            drone = new ARDrone();
            drone.addExceptionListener(new IExceptionListener() {
                public void exceptionOccurred(ARDroneException exc) {
                    exc.printStackTrace();
                }
            });

            drone.start();

            // Tutorial Section 2
//            new TutorialAttitudeListener(drone);

            // Tutorial Section 3
			new TutorialVideoListener(drone);

            // Tutorial Section 4
//			TutorialCommander commander = new TutorialCommander(drone);
//			commander.animateLEDs();
//			commander.takeOffAndLand();
//			commander.leftRightForwardBackward();
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (drone != null)
                drone.stop();

            System.exit(0);
        }
    }
}
