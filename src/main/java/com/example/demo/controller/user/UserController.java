package com.example.demo.controller.user;

import com.example.demo.model.DisplayType;
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

import static com.example.demo.model.ExtractionState.*;

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
    private DisplayType currentOutput = null;

    @GetMapping("")
    public String getUser(Model model) {
        List<Panel> list = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
        boolean isStopped = true;
        if (individualPanelsService.extractionState == RUNNING ||
                contigousPanelsService.extractionState == RUNNING ||
                    mirrorPanelsService.extractionState == RUNNING ){
            isStopped = false;
        }
        model.addAttribute("panels", list);
        model.addAttribute("isStopped", isStopped);
        return "user/user";
    }

    @PostMapping("/togglePanel")
    public ResponseEntity receiveToggleState(@RequestParam("toggleState") boolean toggleState) {
        // Do something with the toggle state
        if(individualPanelsService.extractionState != STOPPED || contigousPanelsService.extractionState != STOPPED || mirrorPanelsService.extractionState != STOPPED){
        } else {
            currentOutput = repositoryService.getSetting().getP_output();
        }
        if (toggleState) {
            switch (currentOutput) {
                case INDIVIDUAL -> {
                    mirrorPanelsService.stop();
                    contigousPanelsService.stop();
                    individualPanelsService.start();
                }
                case CONTIGUOUS -> {
                    mirrorPanelsService.stop();
                    contigousPanelsService.start();
                    individualPanelsService.stop();
                }
                case MIRROR -> {
                    mirrorPanelsService.start();
                    contigousPanelsService.stop();
                    individualPanelsService.stop();
                }
            }
        } else {
            mirrorPanelsService.pause();
            contigousPanelsService.pause();
            individualPanelsService.pause();
        }
        logger.info("returning!");
        return ResponseEntity.ok("hello!");
    }

    @GetMapping("/reset")
    public ResponseEntity<Map<String, String>> reset() {
        Map<String, String> data = new HashMap<>();
        currentOutput = null;
        contigousPanelsService.stop();
        contigousPanelsService.clearAllScreens();
        mirrorPanelsService.stop();
        mirrorPanelsService.clearAllScreens();
        individualPanelsService.stop();
        individualPanelsService.clearAllScreens();
        data.put("Log", "Cleared!");
        return ResponseEntity.ok().body(data);
    }
}
