package dk.localghost.hold17.base.navdata;

import java.util.EventListener;


public interface UltrasoundListener extends EventListener {
    public void receivedRawData(UltrasoundData ud);
}
