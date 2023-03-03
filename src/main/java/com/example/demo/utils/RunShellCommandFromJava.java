package com.example.demo.utils;

import com.example.demo.model.draw.Shape;
import com.example.demo.service.DrawService;
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
import java.util.List;

public class RunShellCommandFromJava {
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    private final int panelByIndex;

    @Autowired
    SerialCommunication serialCommunication;

    public RunShellCommandFromJava(int panelByIndex) {
        this.panelByIndex = panelByIndex;
    }

    public void clearScreen() {
        if (!OSValidator.isWindows()) {
            try {
                serialCommunication.clearPanelAtIndex(panelByIndex);
            } catch (IOException e) {
                logger.error("clearScreen ERROR : " + e);
            }
        }
    }

    public String runCmdForImage2(String filePath, Long duration) {
        File file = new File(filePath);
        try {
            serialCommunication.runSerial(new FileInputStream(file), panelByIndex);
            Thread.sleep(duration * 1000);
        } catch (FileNotFoundException | InterruptedException e) {
            logger.error("runCmdForImage : " + e);
        }
        return "Finished : No Error (IMAGE)";
    }

    public String runCmdForGif2(String gifFilePath, Long duration) {
        GifFrameExtractorService gifFrameExtractorService = new GifFrameExtractorService();
        GifFrameExtractorService.GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
            try {
                if (serialCommunication != null) {
                    serialCommunication.runSerial(FileUtils.asInputStream(frame), panelByIndex);
                }
            } catch (IOException e) {
                logger.error("gifFrameExtractorCallback : " + e);
            }
        };
        return gifFrameExtractorService.extractGifFrames2(gifFilePath, gifFrameExtractorCallback, duration);
    }

    public String runCmdForVideo2(String videoFilePath, Long duration) {
        VideoFrameExtractorService videoFrameExtractorService = new VideoFrameExtractorService();
        VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
            try {
                if (serialCommunication != null && frame != null) {
                    serialCommunication.runSerial(FileUtils.asInputStream(frame), panelByIndex);
                }
            } catch (IOException e) {
                logger.error("videoFrameExtractorCallback : " + e);
            }
        };
        return videoFrameExtractorService.extractVideoFrames2(videoFilePath, 15, videoFrameExtractorCallback, duration);
    }


    public void runCmdForShape(List<Shape> shapes) {
        String s = "";
        for (Shape shape : shapes) {
            if (shape.getType().equalsIgnoreCase("square")) {
                s = DrawService.rect(shape);
            } else if (shape.getType().equalsIgnoreCase("circle")) {
                s = DrawService.circle(shape);
            }
            serialCommunication.runSerial(s, panelByIndex);
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                logger.error("Error : " + e);
            }
        }
    }
}
