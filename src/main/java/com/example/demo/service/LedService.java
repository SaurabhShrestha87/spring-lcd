package com.example.demo.service;

import com.example.demo.model.InfoType;
import com.example.demo.model.Information;
import com.example.demo.model.Panel;
import com.example.demo.utils.RunShellCommandFromJava;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
@Service
@NoArgsConstructor
@Getter
@Setter
public class LedService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LedService.class);
    private static final int INTERVAL_SEND_SECONDS = 33;
    RunShellCommandFromJava runShellCommandFromJava0 = new RunShellCommandFromJava();
    RunShellCommandFromJava runShellCommandFromJava1 = new RunShellCommandFromJava();
    RunShellCommandFromJava runShellCommandFromJava2 = new RunShellCommandFromJava();
    Information information;
    volatile String shFilePath;
    volatile Panel panel;
    ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public void run() {
        try {
            RunShellCommandFromJava runShellCommandFromJava;
//          runShellCommandFromJava0.runShCmd(shFilePath);
            if (panel.getName().equalsIgnoreCase("/dev/ttyACM0")) {
                runShellCommandFromJava = runShellCommandFromJava0;
            } else if (panel.getName().equalsIgnoreCase("/dev/ttyACM1")) {
                runShellCommandFromJava = runShellCommandFromJava1;
            } else if (panel.getName().equalsIgnoreCase("/dev/ttyACM2")) {
                runShellCommandFromJava = runShellCommandFromJava2;
            } else {
                runShellCommandFromJava = runShellCommandFromJava0;
            }
            executor.execute(() -> {
                if (information.getType() == InfoType.GIF) {
                    runShellCommandFromJava.runCmdForGif(information.getName(), information.getUrl(), panel);
                } else {
                    runShellCommandFromJava.runCmdForImage(information.getUrl(), panel);
                }
            });
        } catch (Exception ex) {
            logger.error("Ran Shell Command Error... " + ex.getMessage());
        }
    }

    public void clearScreen(String blankFilePath, List<String> devices) {
        runShellCommandFromJava0.clearScreen(blankFilePath, devices);
        runShellCommandFromJava1.clearScreen(blankFilePath, devices);
        runShellCommandFromJava2.clearScreen(blankFilePath, devices);
    }
}