package com.example.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public void runCmdForGif(String fileName, String filePath, String deviceName) throws IOException {
        logger.info("runCmdForGif STARTED!");
        List<String> gifFrames = new ArrayList<>();
        if (OSValidator.isWindows()) {
        } else {
            try {
                GifDecoder d = new GifDecoder();
                d.read(filePath);
                int n = d.getFrameCount();
                for (int i = 0; i < n; i++) {
                    BufferedImage bFrame = d.getFrame(i);// frame i
                    int delay = d.getDelay(i);  // display duration of frame in milliseconds
                    currentGifDelay = delay;
                    File iframe = new File(fileName + "_frame_" + i + ".png");
                    ImageIO.write(bFrame, "png", iframe);
                    gifFrames.add(iframe.getAbsolutePath());
                    logger.info("iframe getAbsolutePath!" + iframe.getAbsolutePath());
                }
                gifRunning = true;
                while (gifRunning) {
                    for (String gifFrame : gifFrames) {
                        processBuilder.command("bash", "-c", "cat " + gifFrame + " > /dev/" + deviceName);
                        logger.info("COMMAND TO RUN |>>| cat %s > /dev/%s".formatted(gifFrame, deviceName));
                        runProcess();
                    }
                }
            } catch (Exception e) {
                logger.error("ERROR in runCmdForGif! : " + e);
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
