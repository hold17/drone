package dk.localghost.hold17.apps.controlcenter;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JPanel;

import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.IARDrone;

public interface ICCPlugin
{

	public void activate(IARDrone drone);
	public void deactivate();
	
	public String getTitle();
	public String getDescription();
	
	public boolean isVisual();
	public Dimension getScreenSize();
	public Point getScreenLocation();
	public JPanel getPanel();
}
