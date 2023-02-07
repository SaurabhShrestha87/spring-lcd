package com.example.demo;

import com.example.demo.utils.FileUtils;
import com.example.demo.utils.GifDecoder;
import com.example.demo.utils.RunShellCommandFromJava;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);
    public static void main(String[] args) throws IOException {

    }

    @Test
    public void gifConversionTest() throws IOException {
        String filePath = "D:\\upload\\giftest.gif";
        String fileName = "giftest.gif";
        String deviceName = "/dev/ttyACM0";
        List<String> gifFrames = new ArrayList<>();
        int currentGifDelay = 0;
        boolean gifRunning = false;
        GifDecoder d = new GifDecoder();
        logger.info("READ SUCCESS:" + d.read(filePath));
        int frameCounts = d.getFrameCount();
        logger.info("getFrameCount : " + frameCounts);
        for (int frameCount = 0; frameCount < frameCounts; frameCount++) {
            BufferedImage bFrame = d.getFrame(frameCount);
            currentGifDelay = d.getDelay(frameCount);
            String folderName = FileUtils.createGifFramesFolderDir(fileName);
            Files.createDirectories(Path.of(folderName));
            File iframe = new File(FileUtils.createFrameFromCount(folderName,frameCount));
            ImageIO.write(bFrame, "png", iframe);
            gifFrames.add(iframe.getAbsolutePath());
            logger.info("iframe getAbsolutePath!" + iframe.getAbsolutePath());
        }
        gifRunning = true;
        while (gifRunning) {
            for (String gifFrame : gifFrames) {
                logger.info("COMMAND TO RUN |>>| cat %s > /dev/%s".formatted(gifFrame, deviceName));
            }
            gifRunning = false;
        }
    }
}
