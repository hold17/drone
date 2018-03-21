package dk.localghost.hold17.base.navdata;

import java.util.EventListener;


public interface WindListener extends EventListener {

	public void receivedEstimation(WindEstimationData d);

}
