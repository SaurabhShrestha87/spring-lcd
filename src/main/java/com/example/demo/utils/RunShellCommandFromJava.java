package com.example.demo.utils;

import com.example.demo.model.DeviceType;
import com.example.demo.model.Panel;
import com.example.demo.service.SerialCommunication;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.utils.FileUtils.*;
import static com.example.demo.utils.GifDecoder.GifFrameFile;

public class RunShellCommandFromJava extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    private final SerialCommunication serialCommunication;
    private boolean gifRunning = false;

    public RunShellCommandFromJava(DeviceType device) {
        serialCommunication = new SerialCommunication(device);
    }

    public void destroyCmd() {
//        if (process != null && process.isAlive()) {
//            process.destroy();
//        }
        logger.error("TODO : destroyCmd()");
    }

//    public void runShCmd(String pathToShFile) {
//        if (OSValidator.isWindows()) {
//        } else {
//            processBuilder.command(pathToShFile);
//        }
//        serialCommunication.runSerial(FileUtils.readImage(blankFilePath));
//    }

    public void clearAllScreens(String blankFilePath, List<String> devices) {
        for (String device : devices) {
            gifRunning = false;
            logger.info("FILE : " + blankFilePath + " DEVICE :  " + device);
            serialCommunication.runSerial(readImageToInputStream(blankFilePath));
            destroyCmd();
        }
    }

    public void clearScreen(String blankFilePath, Panel panel) {
        logger.info("FILE : " + blankFilePath + " DEVICE :  " + panel.getName());
        serialCommunication.runSerial(readImageToInputStream(blankFilePath));
        destroyCmd();
    }

    public void runCmdForImage(String filePath, Panel panel) {
        if (OSValidator.isWindows()) {
        } else {
            gifRunning = false;
            logger.info("FILE : " + filePath + " DEVICE :  " + panel.getName());
            serialCommunication.runSerial(readImageToInputStream(filePath));
            destroyCmd();
        }
    }

    @SneakyThrows
    public synchronized void runCmdForGif(String fileName, String gifFilePath, Panel panel) {
        List<GifFrameFile> gifFrames = new ArrayList<>();
        if (OSValidator.isWindows()) {
        } else {
            GifDecoder d = new GifDecoder();
            int errorCode = d.read(gifFilePath);
            if (errorCode != 0) {
                gifRunning = false;
                logger.error("READ ERROR:" + errorCode);
            }
            int frameCounts = d.getFrameCount();
            for (int frameCount = 0; frameCount < frameCounts; frameCount++) {
                BufferedImage bFrame = d.getFrame(frameCount);
                String folderName = createGifFramesFolderDir(fileName);
                Files.createDirectories(Path.of(folderName));
                File iframe = new File(createFrameFromCount(folderName, frameCount));
                ImageIO.write(bFrame, "png", iframe);
                gifFrames.add(new GifFrameFile(iframe.getAbsolutePath(), d.getDelay(frameCount)));
                logger.info("iframe getAbsolutePath!" + iframe.getAbsolutePath());
                gifRunning = true;
            }
            while (gifRunning) {
                for (GifFrameFile gifFrame : gifFrames) {
                    logger.info("FILE : " + gifFrame.filePath + " DEVICE :  " + panel.getName());
                    logger.info("DELAY : " + gifFrame.delay);
                    wait(gifFrame.delay);
                    serialCommunication.runSerial(readImageToInputStream(gifFrame.filePath));
                }
            }
        }
    }
}
