package com.example.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RunShellCommandFromJava {
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    Process process;
    ProcessBuilder processBuilder = new ProcessBuilder();
    private int currentGifDelay = 0;
    private boolean gifRunning = false;

    public void destroyCmd() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    public void runShCmd(String pathToShFile) {
        if (OSValidator.isWindows()) {
        } else {
            processBuilder.command(pathToShFile);
        }
        runProcess();
    }

    public void clearScreen(String blankFilePath, List<String> devices) {
        for (String device : devices) {
            processBuilder.command("bash", "-c", "cat " + blankFilePath + " > /dev/" + device);
            logger.info("COMMAND TO RUN |>>| cat %s > /dev/%s".formatted(blankFilePath, device));
            runProcess();
            destroyCmd();
        }
    }

    public void runCmdForImage(String filePath, String deviceName) {
        if (OSValidator.isWindows()) {
        } else {
            logger.info("COMMAND TO RUN |>>| cat %s > /dev/%s".formatted(filePath, deviceName));
            processBuilder.command("bash", "-c", "cat " + filePath + " > /dev/" + deviceName);
            runProcess();
        }
    }

    public void runCmdForGif(String fileName, String filePath, String deviceName) throws IOException, InterruptedException {
        logger.info("runCmdForGif STARTED!");
        List<String> gifFrames = new ArrayList<>();
        logger.info("fileName : " + fileName);
        logger.info("filePath : " + filePath);
        logger.info("deviceName : " + deviceName);
        if (OSValidator.isWindows()) {
        } else {
            GifDecoder d = new GifDecoder();
            int errorCode = d.read(filePath);
            if(errorCode == 0){
                int frameCounts = d.getFrameCount();
                logger.info("getFrameCount : " + frameCounts);
                for (int frameCount = 0; frameCount < frameCounts; frameCount++) {
                    BufferedImage bFrame = d.getFrame(frameCount);
                    currentGifDelay = d.getDelay(frameCount);
                    String folderName = FileUtils.createGifFramesFolderDir(fileName);
                    Files.createDirectories(Path.of(folderName));
                    File iframe = new File(FileUtils.createFrameFromCount(folderName, frameCount));
                    ImageIO.write(bFrame, "png", iframe);
                    gifFrames.add(iframe.getAbsolutePath());
                    logger.info("iframe getAbsolutePath!" + iframe.getAbsolutePath());
                }
                gifRunning = true;
                while (gifRunning) {
                    for (String gifFrame : gifFrames) {
                        processBuilder.command("bash", "-c", "cat " + gifFrame + " > /dev/" + deviceName);
                        logger.info("COMMAND TO RUN |>>| cat %s > /dev/%s".formatted(gifFrame, deviceName));
                        Thread.sleep(currentGifDelay);
                        runProcess();
                    }
                    gifRunning = false;
                }
            }else{
                logger.error("READ ERROR:" + errorCode);
            }
        }
    }

    private void runProcess() {
        try {
            process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                logger.info("BASH CMD SUCCESS!");
                logger.info(String.valueOf(output));
            } else {
                logger.error("BASH CMD FAILURE!");
                //abnormal...
            }
        } catch (IOException | InterruptedException e) {
            logger.error("runProcess FAILURE! Error : %s".formatted(e));
        }
    }
}
