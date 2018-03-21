package dk.localghost.hold17.apps.tutorial;

import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.navdata.AttitudeListener;
import dk.localghost.hold17.base.navdata.BatteryListener;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.navdata.AttitudeListener;
import dk.localghost.hold17.base.navdata.BatteryListener;

public class TutorialAttitudeListener
{

	public TutorialAttitudeListener(IARDrone drone)
	{
		drone.getNavDataManager().addAttitudeListener(new AttitudeListener() {
			
			public void attitudeUpdated(float pitch, float roll, float yaw)
			{
		    	System.out.println("Pitch: " + pitch + " Roll: " + roll + " Yaw: " + yaw);
			}

			public void attitudeUpdated(float pitch, float roll) { }
			public void windCompensation(float pitch, float roll) { }
		});
		
		drone.getNavDataManager().addBatteryListener(new BatteryListener() {
			
			public void batteryLevelChanged(int percentage)
			{
				System.out.println("Battery: " + percentage + " %");
			}
			
			public void voltageChanged(int vbat_raw) { }
		});
	}

}
