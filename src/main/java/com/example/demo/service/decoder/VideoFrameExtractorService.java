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

/**
 * Service for extracting frames from a video file.
 */
@NoArgsConstructor
public class VideoFrameExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(VideoFrameExtractorService.class);
    protected Java2DFrameConverter converter = new Java2DFrameConverter();
    private boolean isPaused = false;
    private boolean isStopped = false;

    /**
     * Starts the extraction of frames from a video file.
     *
     * @param videoFilePath Path to the video file.
     * @param frameRate     Frame rate at which frames should be extracted.
     * @param callback      Callback to handle the extracted frames.
     * @param duration      Duration of the video to extract frames from.
     */
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

    /**
     * Pauses the frame extraction process.
     */
    public void pause() {
        isPaused = true;
    }

    /**
     * Resumes the frame extraction process.
     */
    public void resume() {
        isPaused = false;
    }

    /**
     * Stops the frame extraction process.
     */
    public void stop() {
        isStopped = true;
        isPaused = false;
    }

    /**
     * Callback interface for handling extracted frames.
     */
    public interface VideoFrameExtractorCallback {
        /**
         * Called when a frame is extracted from the video.
         *
         * @param frame     Extracted frame as a BufferedImage.
         * @param timeStamp long of the extracted frame.
         */
        void onFrameExtracted(BufferedImage frame, long timeStamp);
    }
}
