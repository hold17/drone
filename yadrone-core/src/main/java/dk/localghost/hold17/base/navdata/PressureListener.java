package dk.localghost.hold17.base.navdata;

import java.util.EventListener;


public interface PressureListener extends EventListener {

	public void receivedKalmanPressure(KalmanPressureData d);

	public void receivedPressure(Pressure d);

}
