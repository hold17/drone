package dk.localghost.hold17.apps.paperchase.controller;

import org.opencv.core.Core;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class TestCV {
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public BufferedImage readImageFromFile(String Filepath) {
        BufferedImage img = null;

        try {
            InputStream in = getClass().getResourceAsStream(Filepath);
            return img = ImageIO.read(in);
        } catch (IOException e) {
            System.err.println("couldn't load file at: " + Filepath);
        }
        return null;
    }
}
