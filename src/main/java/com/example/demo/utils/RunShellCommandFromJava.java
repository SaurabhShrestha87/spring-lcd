package com.example.demo.utils;

import com.example.demo.model.Panel;
import lombok.SneakyThrows;
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

import static com.example.demo.utils.GifDecoder.*;

public class RunShellCommandFromJava extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    Process process;
    ProcessBuilder processBuilder = new ProcessBuilder();
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
            gifRunning = false;
            processBuilder.command("bash", "-c", "cat " + blankFilePath + " > /dev/" + device);
            runProcess();
            destroyCmd();
        }
    }

    public void runCmdForImage(String filePath, Panel panel) {
        if (OSValidator.isWindows()) {
        } else {
            gifRunning = false;
            processBuilder.command("bash", "-c", "cat " + filePath + " > /dev/" + panel.getName());
            runProcess();
            destroyCmd();
        }
    }

    @SneakyThrows
    public synchronized void runCmdForGif(String fileName, String filePath, Panel panel) {
        List<GifFrameFile> gifFrames = new ArrayList<>();
        if (OSValidator.isWindows()) {
        } else {
            GifDecoder d = new GifDecoder();
            int errorCode = d.read(filePath);
            if (errorCode != 0) {
                logger.error("READ ERROR:" + errorCode);
            }
            int frameCounts = d.getFrameCount();
            for (int frameCount = 0; frameCount < frameCounts; frameCount++) {
                BufferedImage bFrame = d.getFrame(frameCount);
                String folderName = FileUtils.createGifFramesFolderDir(fileName);
                Files.createDirectories(Path.of(folderName));
                File iframe = new File(FileUtils.createFrameFromCount(folderName, frameCount));
                ImageIO.write(bFrame, "png", iframe);
                gifFrames.add(new GifFrameFile(iframe.getAbsolutePath(),  d.getDelay(frameCount)));
                logger.info("iframe getAbsolutePath!" + iframe.getAbsolutePath());
            }
            gifRunning = true;
            while (gifRunning) {
                for (GifFrameFile gifFrame : gifFrames) {
                    processBuilder.command("bash", "-c", "cat " + gifFrame.filePath + " > /dev/" + panel.getName());
                    logger.info("CMD : " +  "cat " + gifFrame.filePath + " > /dev/" + panel.getName());
                    logger.info("DELAY : " +  gifFrame.delay);
                    wait(gifFrame.delay);
                    runProcess();
                }
            }
        }
    }

    private synchronized void runProcess() {
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
                logger.info(String.valueOf(output));
            } else {
                //abnormal...
            }
        } catch (IOException | InterruptedException e) {
            logger.error("runProcess FAILURE! Error : %s".formatted(e));
        }
    }
}
