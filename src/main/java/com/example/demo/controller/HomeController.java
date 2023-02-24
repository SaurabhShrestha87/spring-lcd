package com.example.demo.controller;

import com.example.demo.model.ThreadState;
import com.example.demo.repository.LendRepository;
import com.example.demo.service.MainService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/home")
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private final MainService mainService;
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
        if(mainService.threadState == ThreadState.PAUSED){
            if (toggleState) {
                mainService.resumeAllThreads();
            } else {
                logger.info("toggleState : WAS ALREADY PAUSED");
            }
        } else if(mainService.threadState == ThreadState.RUNNING){
            if (toggleState) {
                logger.info("toggleState : WAS ALREADY RUNNING");
            } else {
                mainService.pauseAllThreads();
            }
        } else if(mainService.threadState == ThreadState.STOPPED){
            if (toggleState) {
                mainService.startAllThreads();
            } else {
                logger.info("toggleState : WAS ALREADY STOPPED");
            }
        } else if(mainService.threadState == ThreadState.READY){
            if (toggleState) {
                mainService.startAllThreads();
            } else {
                logger.info("toggleState : WAS ALREADY RUNNING");
            }
        }
        return ResponseEntity.ok("hello!");
    }
    @GetMapping("/reset")
    public ResponseEntity<Map<String, String>> reset() {
        mainService.stopAllThreads();
        Map<String, String> data = new HashMap<>();
        data.put("Log", mainService.getData());
        return ResponseEntity.ok().body(data);
    }

    @GetMapping("/getData")
    public ResponseEntity<Map<String, String>> getData() {
        Map<String, String> data = new HashMap<>();
        data.put("Log", mainService.getData());
        return ResponseEntity.ok().body(data);
    }

    @GetMapping("/getLogs")
    public ResponseEntity<Map<String, String>> getLogs() {
        Map<String, String> logs = new HashMap<>();
        logs.put("Log", mainService.getLogs());
        return ResponseEntity.ok().body(logs);
    }
}
