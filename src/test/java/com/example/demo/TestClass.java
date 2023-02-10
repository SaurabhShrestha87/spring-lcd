package com.example.demo;

import com.example.demo.model.DeviceType;
import com.example.demo.service.SerialCommunication;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.GifDecoder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.utils.FileUtils.readBufferedData;
import static com.example.demo.utils.GifDecoder.BufferedImageFrame;

public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);

    public static void main(String[] args) throws IOException {

    }

    @Test
    public void readFileTest() {
//        FileUtils.readFile("D:\\upload\\horseImage.png"); // testing .png file
        FileUtils.readFile("D:\\upload\\frame10"); // testing no extension file
    }

    @Test
    public void gifConversionTest() throws IOException {
        String filePath = "D:\\upload\\giftest.gif";
        String deviceName = "/dev/ttyACM0";
        boolean loopRunning = false;
        String runCmdForGifOut;
        List<BufferedImageFrame> bufferedImageList = new ArrayList<>();
        GifDecoder d = new GifDecoder();
        int errorCode = d.read(filePath);
        if (errorCode != 0) {
            loopRunning = false;
            runCmdForGifOut = "READ ERROR:" + errorCode;
            logger.error(runCmdForGifOut);
        }
        int frameCounts = d.getFrameCount();
        for (int frameCount = 0; frameCount < frameCounts; frameCount++) {
            BufferedImage bFrame = d.getFrame(frameCount);
            int delay = d.getDelay(frameCount);
            bufferedImageList.add(new GifDecoder.BufferedImageFrame(bFrame, delay));
            loopRunning = true;
        }
        while (loopRunning) {
            for (GifDecoder.BufferedImageFrame bufferedImage : bufferedImageList) {
                logger.info("Gif bufferedImage DELAY : " + bufferedImage.delay);
                SerialCommunication serialCommunication = new SerialCommunication(DeviceType.DEVICE0);
                serialCommunication.runSerial(readBufferedData(bufferedImage.bufferedImage));
            }
        }
        runCmdForGifOut = "READ SUCCESS : " + errorCode + "\n" + " Gif Running : " + loopRunning + "\n" + " At Device : " + deviceName;
        logger.error(runCmdForGifOut);
    }
}
