package dk.localghost.hold17.apps.paperchase;

import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.VideoChannel;
import dk.localghost.hold17.apps.paperchase.controller.PaperChaseAbstractController;
import dk.localghost.hold17.apps.paperchase.controller.PaperChaseAutoController;
import dk.localghost.hold17.apps.paperchase.controller.PaperChaseKeyboardController;
import dk.localghost.hold17.base.ARDrone;
import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.VideoChannel;

public class PaperChase 
{
	public final static int IMAGE_WIDTH = 640; // 640 or 1280
	public final static int IMAGE_HEIGHT = 360; // 360 or 720
	
	public final static int TOLERANCE = 40;
	
	private IARDrone drone = null;
	private PaperChaseAbstractController autoController;
	private QRCodeScanner scanner = null;
	
	public PaperChase()
	{
		drone = new ARDrone();
		drone.start();
		drone.getCommandManager().setVideoChannel(VideoChannel.VERT);
		
		PaperChaseGUI gui = new PaperChaseGUI(drone, this);
		
		// keyboard controller is always enabled and cannot be disabled (for safety reasons)
		PaperChaseKeyboardController keyboardController = new PaperChaseKeyboardController(drone);
		keyboardController.start();
		
		// auto controller is instantiated, but not started
		autoController = new PaperChaseAutoController(drone);
		
		scanner = new QRCodeScanner();
		scanner.addListener(gui);
		
		drone.getVideoManager().addImageListener(gui);
		drone.getVideoManager().addImageListener(scanner);
	}
	
	public void enableAutoControl(boolean enable)
	{
		if (enable)
		{
			scanner.addListener(autoController);
			autoController.start();
		}
		else
		{
			autoController.stopController();
			scanner.removeListener(autoController); // only auto autoController registers as TagListener
		}
	}
	
	public static void main(String[] args)
	{
		new PaperChase();
	}
	
}
