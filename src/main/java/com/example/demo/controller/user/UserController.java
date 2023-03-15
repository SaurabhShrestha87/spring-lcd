package com.example.demo.controller.user;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.brightness.BrightnessService;
import com.example.demo.service.contigous.ContigousPanelsService;
import com.example.demo.service.individual.IndividualPanelsService;
import com.example.demo.service.mirror.MirrorPanelsService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@NoArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private IndividualPanelsService individualPanelsService;
    @Autowired
    private ContigousPanelsService contigousPanelsService;
    @Autowired
    private MirrorPanelsService mirrorPanelsService;
    @Autowired
    private BrightnessService brightnessService;
    @Autowired
    private RepositoryService repositoryService;
    private boolean inUse;

    @GetMapping("")
    public String getUser(Model model) {
        List<Panel> list = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
        model.addAttribute("panels", list);
        return "user/user";
    }

    @PostMapping("/togglePanel")
    public ResponseEntity receiveToggleState(@RequestParam("toggleState") boolean toggleState) {
        // Do something with the toggle state
        if (toggleState) {
            mirrorPanelsService.stop();
            contigousPanelsService.stop();
            individualPanelsService.start();
        } else {
            individualPanelsService.pause();
        }
        return ResponseEntity.ok("hello!");
    }

    @PostMapping("/togglePanelContiguous")
    public ResponseEntity togglePanelContiguous(@RequestParam("toggleState") boolean toggleState) {
        if (toggleState) {
            mirrorPanelsService.stop();
            individualPanelsService.stop();
            contigousPanelsService.start();
        } else {
            contigousPanelsService.pause();
        }
        return ResponseEntity.ok("hello!");
    }

    @PostMapping("/togglePanelMirror")
    public ResponseEntity togglePanelMirror(@RequestParam("toggleState") boolean toggleState) {
        if (toggleState) {
            mirrorPanelsService.start();
            individualPanelsService.stop();
            contigousPanelsService.stop();
        } else {
            mirrorPanelsService.pause();
        }
        return ResponseEntity.ok("done");
    }

    @GetMapping("/reset")
    public ResponseEntity<Map<String, String>> reset() {
        Map<String, String> data = new HashMap<>();
        contigousPanelsService.stop();
        contigousPanelsService.clearAllScreens();
        mirrorPanelsService.stop();
        mirrorPanelsService.clearAllScreens();
        individualPanelsService.stop();
        individualPanelsService.clearAllScreens();
        data.put("Log", "Cleared!");
        return ResponseEntity.ok().body(data);
    }

    @GetMapping("/getData")
    public ResponseEntity<Map<String, String>> getData() {
        Map<String, String> data = new HashMap<>();
        data.put("Log", "");
        return ResponseEntity.ok().body(data);
    }

    @GetMapping("/getLogs")
    public ResponseEntity<Map<String, String>> getLogs() {
        Map<String, String> logs = new HashMap<>();
        logs.put("Log", "");
        return ResponseEntity.ok().body(logs);
    }

    @PostMapping("/sliderData")
    public ResponseEntity sliderData(@RequestParam("value") int value, @RequestParam("percentage") String percentage) {
        System.out.println("Received slider value: " + value);
        System.out.println("Received slider percentage: " + percentage);
        brightnessService.setBrightness(value);
        return ResponseEntity.ok("done");
    }

    @PostMapping("/singleSliderData")
    public ResponseEntity singleSliderData(@RequestParam("value") int value, @RequestParam("percentage") String percentage,
                                           @RequestParam("panelId") Long panelId) {
        inUse = true;
        brightnessService.setSingleBrightness(panelId, value);
        inUse = false;
        return ResponseEntity.ok("done");
    }
}
