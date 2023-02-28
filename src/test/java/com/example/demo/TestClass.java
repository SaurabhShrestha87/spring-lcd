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

    @Test
    void extractSingleInputImageToMultipleTest() {
        File file = new File("D:\\upload\\arrow.png");
        try {
            List<InputStream> list = FileUtils.splitImageVertically(new FileInputStream(file), 3);
            FileUtils.saveInputStreamsAsImages(list, "D:\\upload\\split", "ArrowSplit_new");
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
