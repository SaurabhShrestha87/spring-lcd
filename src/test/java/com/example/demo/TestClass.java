package com.example.demo;

import com.example.demo.service.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;


public class TestClass {
    @Test
    void extractVideoToFramesTest() {
//        "D:\\upload\\video.mp4"
        VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (BufferedImage frame, long timestamp) -> {
            try {
                if (frame != null) {
                    FileUtils.inputStreamToFIle(FileUtils.asInputStream(frame), timestamp);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        VideoFrameExtractorService gifFrameExtractorService = new VideoFrameExtractorService();
        gifFrameExtractorService.extractVideoFrames("D:\\upload\\videoDemo.mp4", 1, videoFrameExtractorCallback);
    }
}
