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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    @Autowired
    PanelRepository panelRepository;
    Map<String, RunShellCommandFromJava> runShellCommandFromJavas = new HashMap<>();

    @PostConstruct
    public void init() {
        // initialize your monitor here, instance of someService is already injected by this time.
        for (Panel panel : panelRepository.findAllByStatus(PanelStatus.ACTIVE)) {
            DeviceType deviceType = DeviceType.fromString(panel.getDevice());
            RunShellCommandFromJava runShellCommandFromJava = new RunShellCommandFromJava(deviceType);
            runShellCommandFromJavas.put(panel.getDevice(), runShellCommandFromJava);
        }
    }

    public String execute(Information information, Panel panel) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(() -> {
            try {
                if (information.getType() == InfoType.GIF) {
                    (runShellCommandFromJavas.get(panel.getDevice())).runCmdForGif(information.getUrl(), panel);
                } else {
                    (runShellCommandFromJavas.get(panel.getDevice())).runCmdForImage(information.getUrl(), panel);
                }
            } catch (Exception ex) {
                logger.info("LED SERVICE Error : " + ex.getMessage());
            }
        });
        executorService.shutdown();
        return information.getUrl() + " File uploaded successfully AT " + panel.getDevice();
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