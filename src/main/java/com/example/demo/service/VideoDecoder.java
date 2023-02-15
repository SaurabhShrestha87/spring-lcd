package com.example.demo.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.example.demo.service.SerialLoopService.DEFAULTDELAY;

public class VideoDecoder {
    private static final Logger logger = LoggerFactory.getLogger(VideoDecoder.class);
    final FFmpegFrameGrabber grabber;
    Java2DFrameConverter converter = new Java2DFrameConverter();
    int frameRate;
    int frameNumber = 0;

    public VideoDecoder(String videoFilePath) {
        grabber = new FFmpegFrameGrabber(videoFilePath);
        try {
            grabber.start();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    synchronized InputStream extractNextFrame() {
        frameRate = (int) grabber.getFrameRate();
        int delay = 1000/frameRate;
        try {
            grabber.setFrameNumber(frameNumber);
            Frame frame = grabber.grabImage();
            if (frame == null) {
                throw new RuntimeException("Cannot Grab frame");
            }
            BufferedImage bufferedImage = converter.getBufferedImage(frame);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
            logger.info("SENT FRAME #" + frameNumber + " FPS : " + frameRate);
            tryToWait(delay);
            frameNumber++;
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryToWait(int extraDelay) throws InterruptedException {
        long delayDiff = extraDelay - DEFAULTDELAY;
        if (delayDiff > 0) {
            wait(delayDiff);
        }
    }
}
