package dk.localghost.hold17.base.navdata;

import java.util.EventListener;


public interface AltitudeListener extends EventListener {

    public void receivedAltitude(int altitude);

    public void receivedExtendedAltitude(Altitude d);

}
