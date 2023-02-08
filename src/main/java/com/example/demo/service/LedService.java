package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.PanelRepository;
import com.example.demo.utils.RunShellCommandFromJava;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
public class LedService {
    @Autowired
    PanelRepository panelRepository;
    private static final Logger logger = LoggerFactory.getLogger(LedService.class);
    private static final int INTERVAL_SEND_SECONDS = 33;
    Map<String, RunShellCommandFromJava> runShellCommandFromJavas = new HashMap<>();
    volatile String shFilePath;

    @PostConstruct
    public void init(){
        // initialize your monitor here, instance of someService is already injected by this time.
        logger.info("LED SERVICE run() : Started");
        for (Panel panel : panelRepository.findAllByStatus(PanelStatus.ACTIVE)) {
            RunShellCommandFromJava runShellCommandFromJava = new RunShellCommandFromJava(DeviceType.valueOf(panel.getDevice()));
            runShellCommandFromJavas.put(panel.getDevice(), runShellCommandFromJava);
            logger.info("LED SERVICE run() : " + runShellCommandFromJavas.size());
        }
    }

    public void execute(Information information, Panel panel) {
        logger.info("LED SERVICE RUNNING for file " + information.getName() + " at panel " + panel.getDevice());
        logger.info("LED SERVICE runShellCommandFromJavas size : " + runShellCommandFromJavas.size());
        try {
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