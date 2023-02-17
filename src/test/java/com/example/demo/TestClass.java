package com.example.demo;

import com.example.demo.service.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;


public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);

    public static void main(String[] args) {

    }

    @Test
    public void extractVideoToFramesTest() {
//        "D:\\upload\\video.mp4"
        VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (BufferedImage frame, long timestamp) -> {
            logger.error("videoFrameExtractorCallback : " + frame);
            logger.error("videoFrameExtractorCallback RESOLUTION : " + frame.getWidth() + " X " + frame.getHeight());
//            try {
//                if (frame != null) {
//                    FileUtils.inputStreamToFIle(FileUtils.asInputStream(frame), timestamp);
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        };
        VideoFrameExtractorService gifFrameExtractorService = new VideoFrameExtractorService();
        gifFrameExtractorService.extractVideoFrames("D:\\upload\\video.mp4", 15, videoFrameExtractorCallback);
    }
}
