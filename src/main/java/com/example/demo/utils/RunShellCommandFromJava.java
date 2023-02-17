package com.example.demo.utils;

import com.example.demo.model.DeviceType;
import com.example.demo.service.GifFrameExtractorService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.VideoFrameExtractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RunShellCommandFromJava {
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    @Autowired
    protected SerialCommunication serialCommunication;
    GifFrameExtractorService.GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
        try {
            if (serialCommunication != null) {
                serialCommunication.runSerial(FileUtils.asInputStream(frame));
            }
        } catch (IOException e) {
            logger.error("gifFrameExtractorCallback : " + e);
        }
    };
    VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
        try {
            if (serialCommunication != null && frame != null) {
                serialCommunication.runSerial(FileUtils.asInputStream(frame));
            }
        } catch (IOException e) {
            logger.error("videoFrameExtractorCallback : " + e);
        }
    };
    private Thread executionThread;
    private GifFrameExtractorService gifFrameExtractorService;

    public RunShellCommandFromJava(DeviceType device) {
        serialCommunication = new SerialCommunication(device);
    }

    public void clearScreen() {
        clearExecutions();
        if (!OSValidator.isWindows()) {
            try {
                serialCommunication.serial.write("Q/n");
                serialCommunication.serial.write("Q/n");
                serialCommunication.serial.write("Q/n");
                serialCommunication.serial.write("Q/n");
            } catch (IOException e) {
                logger.error("clearScreen ERROR : " + e);
            }
        }
    }

    public void runCmdForImage(String filePath) {
        clearExecutions();
        File file = new File(filePath);
        try {
            serialCommunication.runSerial(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            logger.error("runCmdForImage : " + e);
        }
    }

    public void runCmdForGif(String gifFilePath) {
        clearExecutions();
        executionThread = new Thread(() -> {
            gifFrameExtractorService = new GifFrameExtractorService();
            gifFrameExtractorService.extractGifFrames(gifFilePath, gifFrameExtractorCallback);
        });
        executionThread.start();
    }

    public void runCmdForVideo(String videoFilePath) {
        clearExecutions();
        executionThread = new Thread(() -> {
            VideoFrameExtractorService gifFrameExtractorService = new VideoFrameExtractorService();
            gifFrameExtractorService.extractVideoFrames(videoFilePath, 15, videoFrameExtractorCallback);
        });
        executionThread.start();
    }

    private void clearExecutions() {
//        logger.info("clearExecutions()");
        if (gifFrameExtractorService != null) {
            gifFrameExtractorService.stop();
//            logger.info("gifFrameExtractorService.stop()");
        }
        if (executionThread != null && executionThread.isAlive()) {
//            logger.info("executionThread.interrupt()");
            executionThread.interrupt();
        }
    }

}
