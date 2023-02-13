package com.example.demo;

import com.example.demo.model.DeviceType;
import com.example.demo.service.SerialCommunication;
import com.example.demo.utils.FileUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);

    public static void main(String[] args) throws IOException {

    }

    @Test
    public void readFileTest2() throws IOException {
        SerialCommunication serialCommunication = new SerialCommunication(DeviceType.DEVICE0);
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber("D:\\upload\\video.mp4");
        frameGrabber.start();
        int frameNumber = 0;
        Frame frame;
        Java2DFrameConverter frameConverter = new Java2DFrameConverter();
        while ((frame = frameGrabber.grab()) != null) {
            frameNumber++;
            BufferedImage bufferedImage = frameConverter.getBufferedImage(frame);
            serialCommunication.runSerial(FileUtils.asInputStream(bufferedImage));
            System.out.println("FRAME : " + frameNumber);
        }
        frameGrabber.stop();
    }
    @Test
    public void readFileTest() {
        SerialCommunication serialCommunication = new SerialCommunication(DeviceType.DEVICE0);
        try (FrameGrabber grabber = new FFmpegFrameGrabber("D:\\upload\\video.mp4")) {
            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                try {
                    grabber.start();
                    long frameRate = (long) grabber.getFrameRate();
                    int i = 0;
                    while (true) {
                        BufferedImage frame = converter.convert(grabber.grab());
                        if (frame == null) {
                            System.out.println(" null FRAME : " + i );
                            break;
                        }
                        // Save the frame to a file
                        // ImageIO.write(frame, "png", new File("frame" + i + ".png"));
                        try {
                            serialCommunication.runSerial(FileUtils.asInputStream(frame));
                            System.out.println("FRAME : " + i + " AT FRAME RATE : " + frameRate);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        i++;
                    }
                    grabber.stop();
                } catch (Exception e) {
                    logger.error("extractFrames() Error : " + e);
                }
            }
        } catch (FrameGrabber.Exception e) {
            logger.error("extractFrames() Error : " + e);
        }
    }
}
