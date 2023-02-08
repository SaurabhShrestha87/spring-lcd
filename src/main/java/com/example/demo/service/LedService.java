package com.example.demo.service;

import com.example.demo.model.DeviceType;
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

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
@Service
@NoArgsConstructor
@Getter
@Setter
public class LedService {
    private static final Logger logger = LoggerFactory.getLogger(LedService.class);
    private static final int INTERVAL_SEND_SECONDS = 33;
    RunShellCommandFromJava runShellCommandFromJava0 = new RunShellCommandFromJava(DeviceType.DEVICE0);
    RunShellCommandFromJava runShellCommandFromJava1 = new RunShellCommandFromJava(DeviceType.DEVICE1);
    RunShellCommandFromJava runShellCommandFromJava2 = new RunShellCommandFromJava(DeviceType.DEVICE2);
    Information information;
    volatile String shFilePath;
    Panel panel;

    public String LedServiceDetails() {
        return "LedService{" +
                "information=" + information.getName() +
                ", panel=" + panel.getDevice() +
                '}';
    }

    public void run() {
        try {
            logger.info("LED SERVICE RUNNING for" + LedServiceDetails());
//          runShellCommandFromJava0.runShCmd(shFilePath);
            if (panel.getDevice().equalsIgnoreCase(DeviceType.DEVICE0.toString())) {
                logger.info("Starting Shell Command for runShellCommandFromJava0 " + panel.getName());
                if (information.getType() == InfoType.GIF) {
                    runShellCommandFromJava0.runCmdForGif(information.getName(), information.getUrl(), panel);
                } else {
                    runShellCommandFromJava0.runCmdForImage(information.getUrl(), panel);
                }
            }
            else if (panel.getDevice().equalsIgnoreCase(DeviceType.DEVICE1.toString())) {
                logger.info("Starting Shell Command for runShellCommandFromJava1 " + panel.getName());
                if (information.getType() == InfoType.GIF) {
                    runShellCommandFromJava1.runCmdForGif(information.getName(), information.getUrl(), panel);
                } else {
                    runShellCommandFromJava1.runCmdForImage(information.getUrl(), panel);
                }
            }
            else if (panel.getDevice().equalsIgnoreCase(DeviceType.DEVICE2.toString())) {
                logger.info("Starting Shell Command for runShellCommandFromJava2" + panel.getName());
                if (information.getType() == InfoType.GIF) {
                    runShellCommandFromJava2.runCmdForGif(information.getName(), information.getUrl(), panel);
                } else {
                    runShellCommandFromJava2.runCmdForImage(information.getUrl(), panel);
                }
            }
        } catch (Exception ex) {
            logger.error("LED SERVICE run() Error... " + ex.getMessage());
        }
    }

    public void clearAllScreens(List<Panel> devices) {
        for (Panel device : devices) {
            clearScreen(device);
        }
    }

    public void clearScreen(Panel panel) {
        if (panel.getDevice().equalsIgnoreCase(DeviceType.DEVICE0.toString())) {
            runShellCommandFromJava0.clearScreen();
        } else if (panel.getDevice().equalsIgnoreCase(DeviceType.DEVICE1.toString())) {
            runShellCommandFromJava1.clearScreen();
        } else if (panel.getDevice().equalsIgnoreCase(DeviceType.DEVICE2.toString())) {
            runShellCommandFromJava2.clearScreen();
        } else {
            logger.error("clearScreen(Panel panel) RAN but incorrect panel");
        }
    }
}