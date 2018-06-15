/*
 *
  Copyright (c) <2011>, <Shigeo Yoshida>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
The names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dk.localghost.hold17.base.video;

import dk.localghost.hold17.base.exception.IExceptionListener;
import dk.localghost.hold17.base.exception.VideoException;
import dk.localghost.hold17.base.manager.AbstractTCPManager;
import dk.localghost.hold17.base.utils.ARDronePorts;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class VideoManager extends AbstractTCPManager implements ImageListener {
    private IExceptionListener excListener;

    private VideoDecoder decoder;

    private ArrayList<ImageListener> listener = new ArrayList<ImageListener>();

    public VideoManager(InetAddress inetaddr, VideoDecoder decoder, IExceptionListener excListener) {
        super(inetaddr);
        this.decoder = decoder;
        this.excListener = excListener;
    }

    public void addImageListener(ImageListener listener) {
        this.listener.add(listener);
        if (this.listener.size() == 1)
            decoder.setImageListener(this);
    }

    public void removeImageListener(ImageListener listener) {
        this.listener.remove(listener);
        if (this.listener.size() == 0)
            decoder.setImageListener(null);
    }

    /**
     * Called only by decoder to inform all the other listeners
     */
    public void imageUpdated(BufferedImage image) {
        for (int i = 0; i < listener.size(); i++) {
            listener.get(i).imageUpdated(image);
        }
    }

    public boolean connect(int port) throws IOException {
        if (decoder == null)
            return false;

        return super.connect(port);
    }

    public void reinitialize() {
        System.out.println("VideoManager: reinitialize video stream ...");
        close();
        System.out.println("VideoManager: previous stream closed ...");
        System.out.println("VideoManager: resetting decoder");
        decoder.reset();
        System.out.println("VideoManager: start connecting again ...");
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (decoder == null)
            return;
        try {
            System.out.println("VideoManager: connect ");
            connect(ARDronePorts.VIDEO_PORT);
            System.out.println("VideoManager: tickle ");
            ticklePort(ARDronePorts.VIDEO_PORT);
            // manager.setVideoBitrateControl(VideoBitRateMode.DISABLED); //
            // bitrate set to maximum
            System.out.println("VideoManager: decode ");
            decoder.decode(getInputStream());
        } catch (Exception exc) {
            exc.printStackTrace();
            excListener.exceptionOccurred(new VideoException(exc));
            connectionStateEvent.stateDisconnected();
        }
        close();
    }

    @Override
    public void close() {
        if (decoder == null)
            return;
        super.close();
    }

}
