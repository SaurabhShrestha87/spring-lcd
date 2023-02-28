package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.model.draw.Shape;
import com.example.demo.repository.PanelRepository;
import com.example.demo.utils.RunShellCommandFromJava;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
@Service
@NoArgsConstructor
@Getter
@Setter
public class LedService {
    //    private static final Logger logger = LoggerFactory.getLogger(LedService.class);
    private static final int INTERVAL_SEND_SECONDS = 33;
    @Autowired
    PanelRepository panelRepository;
    Map<String, RunShellCommandFromJava> runShellCommandFromJavas = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        // initialize your monitor here, instance of someService is already injected by this time.
        for (Panel panel : panelRepository.findAllByStatus(PanelStatus.ACTIVE)) {
            DeviceType deviceType = DeviceType.fromString(panel.getDevice());
            RunShellCommandFromJava runShellCommandFromJava = new RunShellCommandFromJava(deviceType);
            runShellCommandFromJavas.put(panel.getDevice(), runShellCommandFromJava);
        }
    }

    public CompletableFuture<ThreadResult> execute(Information information, Panel panel) {
        if (information.getType() == InfoType.VIDEO) {
            return (runShellCommandFromJavas.get(panel.getDevice())).runCmdForVideo(information.getUrl(), Long.valueOf(information.getDuration()));
        } else if (information.getType() == InfoType.GIF) {
            return (runShellCommandFromJavas.get(panel.getDevice())).runCmdForGif(information.getUrl(), Long.valueOf(information.getDuration()));
        } else {
            return (runShellCommandFromJavas.get(panel.getDevice())).runCmdForImage(information.getUrl(), Long.valueOf(information.getDuration()));
        }
    }

    public String executeSync(Information information, Panel panel) {
        if (information.getType() == InfoType.VIDEO) {
            return (runShellCommandFromJavas.get(panel.getDevice())).runCmdForVideo2(information.getUrl(), Long.valueOf(information.getDuration()));
        } else if (information.getType() == InfoType.GIF) {
            return (runShellCommandFromJavas.get(panel.getDevice())).runCmdForGif2(information.getUrl(), Long.valueOf(information.getDuration()));
        } else if (information.getType() == InfoType.IMAGE) {
            return (runShellCommandFromJavas.get(panel.getDevice())).runCmdForImage2(information.getUrl(), Long.valueOf(information.getDuration()));
        } else {
            return "Some Error Occurred during executeSync";
        }
    }

    public String execute(List<Shape> shapes, Panel panel) {
        (runShellCommandFromJavas.get(panel.getDevice())).runCmdForShape(shapes);
        return "Shape uploaded successfully AT " + panel.getDevice();
    }

    public void clearAllScreens() {
        for (RunShellCommandFromJava runShellCommandFromJava : runShellCommandFromJavas.values()) {
            runShellCommandFromJava.clearScreen();
        }
    }
}