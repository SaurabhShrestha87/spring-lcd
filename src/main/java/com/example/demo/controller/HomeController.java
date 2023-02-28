package com.example.demo.controller;

import com.example.demo.model.ThreadState;
import com.example.demo.repository.LendRepository;
import com.example.demo.service.contigous.ContigousPanelsService;
import com.example.demo.service.individual.IndividualPanelsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/home")
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private final IndividualPanelsService individualPanelsService;
    @Autowired
    private final ContigousPanelsService contigousPanelsService;
    private final LendRepository lendRepository;
    private boolean toggleState;

    @GetMapping("")
    public String getHome() {
        return "home/home";
    }

    @PostMapping("/togglePanel")
    public ResponseEntity receiveToggleState(@RequestParam("toggleState") boolean toggleState) {
        // Do something with the toggle state
        logger.info("toggleState : " + toggleState);
        this.toggleState = toggleState;
        if (individualPanelsService.threadState == ThreadState.PAUSED) {
            if (toggleState) {
                individualPanelsService.resumeAllThreads();
            } else {
                logger.info("toggleState : WAS ALREADY PAUSED");
            }
        } else if (individualPanelsService.threadState == ThreadState.RUNNING) {
            if (toggleState) {
                logger.info("toggleState : WAS ALREADY RUNNING");
            } else {
                individualPanelsService.pauseAllThreads();
            }
        } else if (individualPanelsService.threadState == ThreadState.STOPPED) {
            if (toggleState) {
                individualPanelsService.startAllThreads();
            } else {
                logger.info("toggleState : WAS ALREADY STOPPED");
            }
        } else if (individualPanelsService.threadState == ThreadState.READY) {
            if (toggleState) {
                individualPanelsService.startAllThreads();
            } else {
                logger.info("toggleState : WAS ALREADY RUNNING");
            }
        }
        return ResponseEntity.ok("hello!");
    }

    @GetMapping("/reset")
    public ResponseEntity<Map<String, String>> reset() {
        individualPanelsService.stopAllThreads();
        Map<String, String> data = new HashMap<>();
        data.put("Log", individualPanelsService.getData());
        return ResponseEntity.ok().body(data);
    }

    @GetMapping("/getData")
    public ResponseEntity<Map<String, String>> getData() {
        Map<String, String> data = new HashMap<>();
        data.put("Log", individualPanelsService.getData());
        return ResponseEntity.ok().body(data);
    }

    @GetMapping("/getLogs")
    public ResponseEntity<Map<String, String>> getLogs() {
        Map<String, String> logs = new HashMap<>();
        logs.put("Log", individualPanelsService.getLogs());
        return ResponseEntity.ok().body(logs);
    }
    @PostMapping("/togglePanelContiguous")
    public ResponseEntity togglePanelContiguous(@RequestParam("toggleState") boolean toggleState) {
        if (toggleState)
        return ResponseEntity.ok(contigousPanelsService.start());
        else
        return ResponseEntity.ok(contigousPanelsService.stop());
    }
}
