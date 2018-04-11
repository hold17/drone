package dk.localghost.hold17.base.navdata;

import java.util.EventListener;


public interface VideoListener extends EventListener {

    public void receivedHDVideoStreamData(HDVideoStreamData d);

    public void receivedVideoStreamData(VideoStreamData d);

}
