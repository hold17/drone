package dk.localghost.hold17.autonomous_drone.opencv_processing;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.file.Paths;

public class FilterHelper {

    static {
        nu.pattern.OpenCV.loadShared(); // loading maven version of OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Save matrix to file
     *
     * @param fileName path and appending filename
     * @param mat      The mat of an image
     */
    public static void saveFile(String fileName, Mat mat) {
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
    public static Mat openFile(String fileName) {
        try {
            final String path = Paths.get("").toAbsolutePath().toString();
            final String filePath = (path + "/NewDroneImages/" + fileName).replace('/', '\\');

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
     */
    public static Mat bufferedImageToMat(BufferedImage img) {
        byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    /***
     * Needed for drone video feed
     * @param mat Mat
     * @return BufferedImage
     */
    public static BufferedImage matToBufferedImage(Mat mat) {
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        mat.get(0, 0, data);
        return image;
    }

}