package com.example.demo.service;

import lombok.NoArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

@Service
@NoArgsConstructor
public class VideoFrameExtractorService {
    protected Java2DFrameConverter converter = new Java2DFrameConverter();

    public void extractVideoFrames(String videoFilePath, int frameRate, VideoFrameExtractorCallback callback) {
        try (FrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath)) {
            int delay = 1000 / frameRate;
            try (Frame outputFrame = new Frame(30, 118, Frame.DEPTH_UBYTE, 3)) {
                try {
                    grabber.start();
                    int frameCount = grabber.getLengthInFrames();
                    long timestamp = 0;
                    for (int i = 0; i < frameCount; i++) {
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
                    grabber.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface VideoFrameExtractorCallback {
        void onFrameExtracted(BufferedImage frame, long timeStamp);
    }
}
