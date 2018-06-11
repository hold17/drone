package dk.localghost.hold17.autonomous_drone.opencv_processing.filter;

import dk.localghost.hold17.autonomous_drone.opencv_processing.Direction;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class FilterHelper {

    // TODO: Skift værdierne der tjekkes for, så de passer til dronens kameraopløsning
    public Direction findPaperPosition(Rect rect) {
        if (rect.x > 0 && rect.x < 512) {
            return Direction.LEFT;
        }

        if (rect.x > 512 && rect.x < 768) {
            return Direction.CENTER;
        }

        if (rect.x > 768 && rect.x < 1280) {
            return Direction.RIGHT;
        }
        return Direction.UNKNOWN;
    }

    /**
     * Save matrix to file
     *
     * @param fileName path and appending filename
     * @param mat      The mat of an image
     */
    public void saveFile(String fileName, Mat mat) {
        final String path = Paths.get("").toAbsolutePath().toString();
        final String filePath = (path + "/DroneImagesFiltered/" + fileName).replace('/', '\\');
        Imgcodecs.imwrite(filePath, mat);
        System.out.println("File saved to " + filePath);
    }

    /***
     * Open file as matrix
     * @param fileName file
     * @return Mat
     */
    public Mat openFile(String fileName) {
        try {
            final String path = Paths.get("").toAbsolutePath().toString();
            final String filePath = (path + "/DroneImages/" + fileName).replace('/', '\\');

            Mat newImage = Imgcodecs.imread(filePath);
            if (newImage.dataAddr() == 0) {
                throw new Exception("Couldn't open file " + filePath);
            }

            return newImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * Needed for drone video feed
     * @param img BufferedImage
     * @return Mat
     * @throws IOException
     */
    public Mat bufferedImageToMat(BufferedImage img) {
        try (ByteArrayOutputStream outstream = new ByteArrayOutputStream()) {
            ImageIO.write(img, "jpg", outstream);
            outstream.flush();
            return Imgcodecs.imdecode(new MatOfByte(outstream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * Needed for drone video feed
     * @param mat Mat
     * @return BufferedImage
     */
    public BufferedImage matToBufferedImage(Mat mat) {
        try {
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, mob);
            return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
