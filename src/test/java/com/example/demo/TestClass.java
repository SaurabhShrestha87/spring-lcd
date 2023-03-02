package com.example.demo;

import com.example.demo.service.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;


public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);
    @Test
    void extractVideoToFramesTest() {
//        "D:\\upload\\video.mp4"
        VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (BufferedImage frame, long timestamp) -> {
            try {
                if (frame != null) {
                    FileUtils.inputStreamToFIle(FileUtils.asInputStream(frame), timestamp);
                }
            } catch (IOException e) {
                logger.error("Error : " + e);
            }
        };
        VideoFrameExtractorService gifFrameExtractorService = new VideoFrameExtractorService();
        gifFrameExtractorService.extractVideoFrames("D:\\upload\\videoDemo.mp4", 1, videoFrameExtractorCallback);
    }

    @Test
    void extractSingleInputImageToMultipleTest() {
        File file = new File("D:\\upload\\frame09.png");
        try {
            InputStream[] list = FileUtils.splitInputStreamHorizontally(new FileInputStream(file), 3);
            FileUtils.saveInputStreamsAsImages(list, "D:\\upload\\split", "ArrowSplit_new");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
