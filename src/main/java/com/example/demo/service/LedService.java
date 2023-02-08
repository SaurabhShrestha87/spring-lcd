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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, RunShellCommandFromJava> runShellCommandFromJavas = new HashMap<String, RunShellCommandFromJava>();
    Information information;
    volatile String shFilePath;
    Panel panel;
    private List<Panel> activePanel;

    @Autowired
    public LedService(List<Panel> allByStatus) {
        this.activePanel = allByStatus;
    }

    @Override
    public void run() {
        for (Panel panel : activePanel) {
            RunShellCommandFromJava runShellCommandFromJava = new RunShellCommandFromJava(DeviceType.valueOf(panel.getDevice()));
            runShellCommandFromJavas.put(panel.getDevice(), runShellCommandFromJava);
        }
    }

    public void execute() {
        try {
            logger.info("LED SERVICE RUNNING for file " + information.getName() + " at panel " + panel.getDevice());
//          runShellCommandFromJava0.runShCmd(shFilePath);
            if (information.getType() == InfoType.GIF) {
                (runShellCommandFromJavas.get(panel.getDevice())).runCmdForGif(information.getName(), information.getUrl(), panel);
            } else {
                (runShellCommandFromJavas.get(panel.getDevice())).runCmdForImage(information.getUrl(), panel);
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
        (runShellCommandFromJavas.get(panel.getDevice())).clearScreen();
    }
}