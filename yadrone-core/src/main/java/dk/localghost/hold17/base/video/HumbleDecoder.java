/*******************************************************************************
 * Copyright (c) 2014, Art Clarke.  All rights reserved.
 *
 * This file is part of Humble-Video.
 *
 * Humble-Video is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Humble-Video is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Humble-Video.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package dk.localghost.hold17.base.video;

import io.humble.video.*;
import io.humble.video.Container;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import io.humble.video.customio.HumbleIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class HumbleDecoder implements VideoDecoder {
    private ImageListener listener;
    private volatile boolean doStop = false;

    /**
     * Opens a file, and plays the video from it on a screen at the right rate.
     * @param is The inputstream to play.
     */
    @Override
    public synchronized void decode(InputStream is) throws InterruptedException, IOException {
        /*
         * Start by creating a container object, in this case a demuxer since
         * we are reading, to get video data from.
         */
        Demuxer demuxer = Demuxer.make();

        /*
         * Open the demuxer with the filename passed on.
         */
//        demuxer.setFlag(Demuxer.Flag.FLA, true);
        demuxer.open(HumbleIO.map(is), DemuxerFormat.findFormat("h264"), false, true, null, null);


        /*
         * Query how many streams the call to open found
         */
        int numStreams = demuxer.getNumStreams();

        /*
         * Iterate through the streams to find the first video stream
         */
        int videoStreamId = -1;
        long streamStartTime = Global.NO_PTS;
        Decoder videoDecoder = null;
        for(int i = 0; i < numStreams; i++)
        {
            final DemuxerStream stream = demuxer.getStream(i);
            streamStartTime = stream.getStartTime();
            final Decoder decoder = stream.getDecoder();
            if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
                videoStreamId = i;
                videoDecoder = decoder;
                // stop at the first one.
                break;
            }
        }
        if (videoStreamId == -1)
            throw new RuntimeException("could not find video stream in container");

        /*
         * Now we have found the video stream in this file.  Let's open up our decoder so it can
         * do work.
         */
        KeyValueBag keyValueBag = KeyValueBag.make();

//        keyValueBag.setValue("minrate", "5000");
//        keyValueBag.setValue("maxrate", "10000");

//        keyValueBag.setValue("skip_frame", "0");
//        keyValueBag.setValue("bug", "1");
//        keyValueBag.setValue("skip_idct", "0");
//        keyValueBag.setValue("ec", "2");
//        keyValueBag.setValue("ec", "1");
//        keyValueBag.setValue("err_detect", "1");

//        videoDecoder.setFlag(Coder.Flag.FLAG_BITEXACT, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_INPUT_PRESERVED, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_NORMALIZE_AQP, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_LOOP_FILTER, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_4MV, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_INPUT_PRESERVED, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_BITEXACT, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_AC_PRED, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_CLOSED_GOP, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_EMU_EDGE, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_GMC, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_INTERLACED_DCT, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_INTERLACED_ME, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_MV0, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_PASS1, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_PASS2, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_QPEL, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_QSCALE, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_TRUNCATED, true);
//        videoDecoder.setFlag(Coder.Flag.FLAG_UNALIGNED, true);
        videoDecoder.setFlag(Coder.Flag.FLAG_PSNR, true);
        videoDecoder.setFlag(Coder.Flag.FLAG_LOW_DELAY, true);
        videoDecoder.setFlag2(Coder.Flag2.FLAG2_FAST, true);
        videoDecoder.setFlag2(Coder.Flag2.FLAG2_CHUNKS, true);

        videoDecoder.open(keyValueBag, null);


        final MediaPicture picture = MediaPicture.make(
                videoDecoder.getWidth(),
                videoDecoder.getHeight(),
                videoDecoder.getPixelFormat());

        /** A converter object we'll use to convert the picture in the video to a BGR_24 format that Java Swing
         * can work with. You can still access the data directly in the MediaPicture if you prefer, but this
         * abstracts away from this demo most of that byte-conversion work. Go read the source code for the
         * converters if you're a glutton for punishment.
         */
        final MediaPictureConverter converter =
                MediaPictureConverterFactory.createConverter(
                        MediaPictureConverterFactory.HUMBLE_BGR_24,
                        picture);

        BufferedImage image = null;

//        MediaResampler resampler = MediaPictureResampler.make();

        /**
         * Media playback, like comedy, is all about timing. Here we're going to introduce <b>very very basic</b>
         * timing. This code is deliberately kept simple (i.e. doesn't worry about A/V drift, garbage collection pause time, etc.)
         * because that will quickly make things more complicated.
         *
         * But the basic idea is there are two clocks:
         * <ul>
         * <li>Player Clock: The time that the player sees (relative to the system clock).</li>
         * <li>Stream Clock: Each stream has its own clock, and the ticks are measured in units of time-bases</li>
         * </ul>
         *
         * And we need to convert between the two units of time. Each MediaPicture and MediaAudio object have associated
         * time stamps, and much of the complexity in video players goes into making sure the right picture (or sound) is
         * seen (or heard) at the right time. This is actually very tricky and many folks get it wrong -- watch enough
         * Netflix and you'll see what I mean -- audio and video slightly out of sync. But for this demo, we're erring for
         * 'simplicity' of code, not correctness. It is beyond the scope of this demo to make a full fledged video player.
         */

        // Calculate the time BEFORE we start playing.
        long systemStartTime = System.nanoTime();
        // Set units for the system time, which because we used System.nanoTime will be in nanoseconds.
        final Rational systemTimeBase = Rational.make(1, 1000000000);
        // All the MediaPicture objects decoded from the videoDecoder will share this timebase.
        final Rational streamTimebase = videoDecoder.getTimeBase();

        /**
         * Now, we start walking through the container looking at each packet. This
         * is a decoding loop, and as you work with Humble you'll write a lot
         * of these.
         *
         * Notice how in this loop we reuse all of our objects to avoid
         * reallocating them. Each call to Humble resets objects to avoid
         * unnecessary reallocation.
         */
        final MediaPacket packet = MediaPacket.make();
        while(!doStop && demuxer.read(packet) >= 0) {
            /**
             * Now we have a packet, let's see if it belongs to our video stream
             */
            if (packet.getStreamIndex() == videoStreamId)
            {
                /**
                 * A packet can actually contain multiple sets of samples (or frames of samples
                 * in decoding speak).  So, we may need to call decode  multiple
                 * times at different offsets in the packet's data.  We capture that here.
                 */
                int offset = 0;
                int bytesRead = 0;
                do {
                    bytesRead += videoDecoder.decode(picture, packet, offset);
                    if (picture.isComplete()) {
                        image = displayVideoAtCorrectTime(streamStartTime, picture,
                                converter, image, systemStartTime, systemTimeBase,
                                streamTimebase);

                        // display it on the Java Swing window
                        if (listener != null) {
                            byte[] temp = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                            byte[] temp2 = Arrays.copyOfRange(temp, 0, 2764800);
                            BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                            img.setData(Raster.createRaster(img.getSampleModel(), new DataBufferByte(temp2, temp2.length), new Point()));

                            listener.imageUpdated(img);
                        }
                    }
                    offset += bytesRead;
                } while (offset < packet.getSize());
            }
        }

        // Some video decoders (especially advanced ones) will cache
        // video data before they begin decoding, so when you are done you need
        // to flush them. The convention to flush Encoders or Decoders in Humble Video
        // is to keep passing in null until incomplete samples or packets are returned.
        do {
            videoDecoder.decode(picture, null, 0);
            if (picture.isComplete()) {
                image = displayVideoAtCorrectTime(streamStartTime, picture, converter,
                        image, systemStartTime, systemTimeBase, streamTimebase);
            }
        } while (picture.isComplete());

        // It is good practice to close demuxers when you're done to free
        // up file handles. Humble will EVENTUALLY detect if nothing else
        // references this demuxer and close it then, but get in the habit
        // of cleaning up after yourself, and your future girlfriend/boyfriend
        // will appreciate it.
        demuxer.close();
    }

    /**
     * Takes the video picture and displays it at the right time.
     */
    private BufferedImage displayVideoAtCorrectTime(long streamStartTime,
                                                           final MediaPicture picture, final MediaPictureConverter converter,
                                                           BufferedImage image, long systemStartTime,
                                                           final Rational systemTimeBase, final Rational streamTimebase)
            throws InterruptedException {
        long streamTimestamp = picture.getTimeStamp();
        // convert streamTimestamp into system units (i.e. nano-seconds)
        streamTimestamp = systemTimeBase.rescale(streamTimestamp-streamStartTime, streamTimebase);
        // get the current clock time, with our most accurate clock
        long systemTimestamp = System.nanoTime();
        // loop in a sleeping loop until we're within 1 ms of the time for that video frame.
        // a real video player needs to be much more sophisticated than this.
        while (streamTimestamp > (systemTimestamp - systemStartTime + 1000000)) {
            Thread.sleep(1);
            systemTimestamp = System.nanoTime();
        }
        // finally, convert the image from Humble format into Java images.
        image = converter.toImage(image, picture);

        return image;
    }

    public void stop() {
        doStop = true;
    }

    public void setImageListener(ImageListener listener) {
        this.listener = listener;
    }

    @Override
    public void reset() {
        stop();
        //wait until the method "decode" ends (on another thread).
        synchronized (this) {
            //reset:
            doStop = false;
        }
    }
}