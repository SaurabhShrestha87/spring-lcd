package com.example.demo;

import com.example.demo.service.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;


public class TestClass {

    @Test
    void extractVideoToFramesTest() {
//        "E:\\upload\\video.mp4"
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
        gifFrameExtractorService.extractVideoFrames("E:\\upload\\videoDemo.mp4", 1, videoFrameExtractorCallback);
    }

    @Test
    void extractSingleInputImageToMultipleTest() {
        File file = new File("E:\\upload\\frame09.png");
        try {
            List<InputStream> list = FileUtils.splitInputStreamHorizontally(new FileInputStream(file), 3);
            FileUtils.saveInputStreamsAsImages(list, "E:\\upload\\split", "ArrowSplit_new");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
