package dk.localghost.hold17.base.video;

import java.io.IOException;
import java.io.InputStream;

public interface VideoDecoder {
    void decode(InputStream is) throws InterruptedException, IOException;
    void stop();
    void setImageListener(ImageListener listener);
    void reset();
}
