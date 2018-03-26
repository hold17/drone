package dk.localghost.hold17.apps.controlcenter;

import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.video.HumbleDecoder;
import dk.localghost.hold17.base.video.VideoDecoder;


public class YADroneControlCenter {
    private ARDrone ardrone = null;

    public YADroneControlCenter() {
        initialize();
    }

    private void initialize() {
        try {
            //ardrone = new ARDrone("10.0.1.2");
            ardrone = new ARDrone("10.0.1.2", new HumbleDecoder());
            System.out.println("Connect drone controller");
            ardrone.start();

            new CCFrame(ardrone);
        } catch (Exception exc) {
            exc.printStackTrace();

            if (ardrone != null)
                ardrone.stop();
            System.exit(-1);
        }
    }

    public static void main(String args[]) {
        new YADroneControlCenter();
    }
}