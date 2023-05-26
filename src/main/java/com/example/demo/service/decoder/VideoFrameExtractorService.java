package com.example.demo.service.decoder;

import lombok.NoArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

@NoArgsConstructor
public class VideoFrameExtractorService {

    /**
     Starts the video extraction process.
     @param videoFilePath The path of the video file to extract frames from.
     @param frameRate The desired frame rate for extracting frames.
     @param callback The callback interface to receive the extracted frames.
     @param duration The duration (in seconds) for which frames should be extracted.
     */

    private static final Logger logger = LoggerFactory.getLogger(VideoFrameExtractorService.class);
    protected Java2DFrameConverter converter = new Java2DFrameConverter();
    private boolean isPaused = false;
    private boolean isStopped = false;

    public void start_vid_extraction(String videoFilePath, int frameRate, VideoFrameExtractorCallback callback, Long duration) {
        duration = duration * 1000;
        long totalTime = 0;
        try (FrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath)) {
            int delay = 1000 / frameRate;
            try (Frame outputFrame = new Frame(30, 118, Frame.DEPTH_UBYTE, 3)) {
                try {
                    grabber.start();
                    int frameCount = grabber.getLengthInFrames();
                    while ((duration.compareTo(totalTime) > 0)) {
                        if (isStopped) {
                            break;
                        }
                        if (isPaused) {
                            Thread.sleep(1000);
                            continue;
                        }
                        long timestamp = 0;
                        for (int i = 0; i < frameCount; i++) {
                            if (isStopped) {
                                break;
                            }
                            if (isPaused) {
                                i--;
                                Thread.sleep(1000);
                                continue;
                            }
                            if (duration.compareTo(timestamp) < 0) {
                                totalTime += timestamp;
                                break;
                            }
                            Frame frame = grabber.grab();
                            if (frame == null) {
                                break;
                            }
                            timestamp += delay;
                            frame.timestamp = timestamp;
                            BufferedImage bufferedImage = converter.getBufferedImage(frame);
                            if (bufferedImage != null) {
                                if (grabber.getImageWidth() == 30 && grabber.getImageWidth() == 30) {
                                    callback.onFrameExtracted(bufferedImage, timestamp);
                                    Thread.sleep(delay);
                                } else {
                                    // Create a new BufferedImage with the desired resolution
                                    BufferedImage outputImage = new BufferedImage(30, 118, BufferedImage.TYPE_BYTE_GRAY);
                                    // Resize the input image to the new resolution
                                    Graphics2D g2d = outputImage.createGraphics();
                                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                    g2d.drawImage(bufferedImage, 0, 0, 30, 118, null);
                                    g2d.dispose();
                                    // Convert the resized BufferedImage back to a Buffer[]
                                    // Convert the resized BufferedImage to a ByteBuffer
                                    ByteBuffer outputData = ByteBuffer.wrap(((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData());
                                    // Assign the ByteBuffer to the output frame
                                    outputFrame.image[0] = outputData;
                                    callback.onFrameExtracted(outputImage, timestamp);
                                    Thread.sleep(delay);
                                }
                            }
                        }
                        totalTime += timestamp;
                    }
                    grabber.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FrameGrabber.Exception e) {
            logger.error("Error : " + e);
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public void stop() {
        isStopped = true;
        isPaused = false;
    }

    public interface VideoFrameExtractorCallback {
        void onFrameExtracted(BufferedImage frame, long timeStamp);
    }
}
