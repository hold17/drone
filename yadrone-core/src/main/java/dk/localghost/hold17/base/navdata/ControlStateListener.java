package dk.localghost.hold17.base.navdata;

import java.util.EventListener;


public interface ControlStateListener  extends EventListener {
	public void controlStateChanged(ControlState state);
}
