package com.example.demo.utils;

import com.example.demo.DemoApplication;
import com.example.demo.model.DeviceType;
import com.example.demo.model.Panel;
import com.example.demo.service.SerialCommunication;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.utils.FileUtils.readBufferedData;
import static com.example.demo.utils.FileUtils.readFile;
import static com.example.demo.utils.GifDecoder.BufferedImageFrame;

public class RunShellCommandFromJava {
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    private final SerialCommunication serialCommunication;
    private boolean loopRunning = false;

    public RunShellCommandFromJava(DeviceType device) {
        serialCommunication = new SerialCommunication(device);
    }

    public void clearScreen() {
        serialCommunication.runSerial(readFile(DemoApplication.blankFilePath));
        loopRunning = false;
    }

    public synchronized void runCmdForImage(String filePath, Panel panel) {
        String runCmdForImageOut = "FILE : " + filePath + " DEVICE :  " + panel.getName();
        logger.info(runCmdForImageOut);
        serialCommunication.runSerial(readFile(filePath));
    }

    @SneakyThrows
    public synchronized void runCmdForGif(String gifFilePath, Panel panel) {
        String runCmdForGifOut;
        List<BufferedImageFrame> bufferedImageList = new ArrayList<>();
        GifDecoder decoder = new GifDecoder();
        int errorCode = decoder.read(gifFilePath);
        if (errorCode != 0) {
            loopRunning = false;
            runCmdForGifOut = "READ ERROR:" + errorCode;
            logger.error(runCmdForGifOut);
        }
        int frameCounts = decoder.getFrameCount();
        for (int frameCount = 0; frameCount < frameCounts; frameCount++) {
            BufferedImage bFrame = decoder.getFrame(frameCount);
            int delay = decoder.getDelay(frameCount);
            bufferedImageList.add(new GifDecoder.BufferedImageFrame(bFrame, delay));
            loopRunning = true;
        }
        while (loopRunning) {
            for (GifDecoder.BufferedImageFrame bufferedImage : bufferedImageList) {
                logger.info("Gif bufferedImage DELAY : " + bufferedImage.delay);
                wait(bufferedImage.delay * 100L);
                serialCommunication.runSerial(readBufferedData(bufferedImage.bufferedImage));
            }
        }
        runCmdForGifOut = "READ SUCCESS : " + errorCode + "\n" + " Gif Running : " + loopRunning + "\n" + " At Device : " + panel.getDevice();
        logger.error(runCmdForGifOut);
    }

}
