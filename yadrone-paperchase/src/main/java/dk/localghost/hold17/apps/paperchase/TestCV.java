package dk.localghost.hold17.apps.paperchase;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class TestCV {
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public TestCV() {
        try {
            Mat testMat = openFile("a4-papir.jpg");
            System.out.println(testMat.toString());
        } catch (Exception e) {
            System.err.println("Something went wrong" + e.toString());
        }
    }

    public static void main(String[] args) {
        new TestCV();
    }

    public Mat openFile(String fileName) throws Exception{
        final String path = Paths.get("").toAbsolutePath().toString();
        final String filePath = (path + "/TestImages/" + fileName).replace('/', '\\');

        Mat newImage = Imgcodecs.imread(filePath);
        if(newImage.dataAddr()==0){
            throw new Exception ("Couldn't open file "+filePath);
        }
        return newImage;
    }

    public Mat bufferedImageToMat(BufferedImage img) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", outstream);
        outstream.flush();
        return Imgcodecs.imdecode(new MatOfByte(outstream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    public BufferedImage matToBufferedImage(Mat mat) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }
}
