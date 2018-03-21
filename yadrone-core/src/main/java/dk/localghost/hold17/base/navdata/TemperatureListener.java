package dk.localghost.hold17.base.navdata;

import java.util.EventListener;


public interface TemperatureListener extends EventListener {

	void receivedTemperature(Temperature d);

}
