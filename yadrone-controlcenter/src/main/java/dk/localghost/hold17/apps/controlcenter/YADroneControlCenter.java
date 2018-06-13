package dk.localghost.hold17.apps.controlcenter;

import dk.localghost.hold17.base.ARDrone;


public class YADroneControlCenter {
    private ARDrone ardrone = null;

    public YADroneControlCenter() {
        initialize();
    }

    private void initialize() {
        try {
            //ardrone = new ARDrone();
            ardrone = new ARDrone("192.168.1.1");
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