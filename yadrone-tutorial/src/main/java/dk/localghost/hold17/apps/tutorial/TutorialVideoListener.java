package dk.localghost.hold17.apps.tutorial;

import dk.localghost.hold17.base.IARDrone;
import dk.localghost.hold17.base.command.VideoChannel;
import dk.localghost.hold17.base.video.ImageListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class TutorialVideoListener extends JFrame {
    private BufferedImage image = null;

    public TutorialVideoListener(final IARDrone drone) {
        super("YADrone Tutorial");

        setSize(640, 360);
        setVisible(true);

        drone.getVideoManager().addImageListener(new ImageListener() {
            public void imageUpdated(BufferedImage newImage) {
                image = newImage;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                drone.getCommandManager().setVideoChannel(VideoChannel.NEXT);
            }
        });

        // close the 
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                drone.stop();
                System.exit(0);
            }
        });
    }

    public synchronized void paint(Graphics g) {
        if (image != null)
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
    }
}
