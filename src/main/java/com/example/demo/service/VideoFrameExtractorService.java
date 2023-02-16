package com.example.demo.service;

import lombok.NoArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;

@Service
@NoArgsConstructor
public class VideoFrameExtractorService {
    Java2DFrameConverter converter = new Java2DFrameConverter();

    public void extractVideoFrames(String videoFilePath, int frameRate, VideoFrameExtractorCallback callback) {
            FrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath);
            int delay = 1000 / frameRate;
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
                    callback.onFrameExtracted(bufferedImage);
                    Thread.sleep(delay);
                }
                grabber.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

        public interface VideoFrameExtractorCallback {
            void onFrameExtracted(BufferedImage frame);
        }
    }
