package dk.localghost.hold17.apps.paperchase.controller;

import com.google.zxing.Result;

import dk.localghost.hold17.apps.paperchase.TagListener;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.apps.paperchase.TagListener;
import dk.localghost.hold17.base.IARDrone;

public abstract class PaperChaseAbstractController extends Thread implements TagListener
{
	protected boolean doStop = false;

	protected IARDrone drone;
	
	public PaperChaseAbstractController(IARDrone drone)
	{
		this.drone = drone;
	}

	public abstract void run();
	
	public void onTag(Result result, float orientation)
	{

	}
	
	public void stopController()
	{
		doStop = true;
	}
}
