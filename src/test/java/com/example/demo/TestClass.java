package com.example.demo;

import com.example.demo.model.DeviceType;
import com.example.demo.service.SerialCommunication;
import com.example.demo.utils.FileUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);

    public static void main(String[] args) throws IOException {

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
