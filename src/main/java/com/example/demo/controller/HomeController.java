package com.example.demo.controller;

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
public class HomeController implements MainService.MainServiceCallback {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private final MainService mainService;
    private final LendRepository lendRepository;
    Map<String, String> data = new HashMap<>();
    Map<String, String> logs = new HashMap<>();
    private boolean toggleState;

    @PostConstruct
    void init() {
        mainService.setCallback(this);
    }

    @GetMapping("")
    public String getHome() {
        return "home/home";
    }

    @PostMapping("/togglePanel")
    public ResponseEntity receiveToggleState(@RequestParam("toggleState") boolean toggleState) {
        // Do something with the toggle state
        logger.info("toggleState : " + toggleState);
        this.toggleState = toggleState;
        if (toggleState) {
            mainService.startLoop();
        } else {
            mainService.pauseLoop();
        }
        return ResponseEntity.ok("hello!");
    }

    @GetMapping("/getData")
    public ResponseEntity<Map<String, String>> getData() {
        logger.info("getData data=" + data);
        return ResponseEntity.ok().body(data);
    }

    @GetMapping("/getLogs")
    public ResponseEntity<Map<String, String>> getLogs() {
        Map<String, String> logs = new HashMap<>();
        logs.put("Log", mainService.getLogs());
        return ResponseEntity.ok().body(logs);
    }

    @Override
    public void currentInformationOnPanel(String infoString, String panelString) {
        data.putIfAbsent(panelString, infoString);
    }
}
