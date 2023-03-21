package com.example.demo.controller.user;

import com.example.demo.model.DisplayType;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.brightness.BrightnessService;
import com.example.demo.service.contigous.ContigousPanelsService;
import com.example.demo.service.individual.IndividualPanelsService;
import com.example.demo.service.mirror.MirrorPanelsService;
import com.example.demo.service.settings.IdentifyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/user/panel")
public class UserPanelController {
    private static final Logger logger = LoggerFactory.getLogger(UserPanelController.class);
    @Autowired
    private final RepositoryService repositoryService;
    @Autowired
    private final BrightnessService brightnessService;
    private List<Panel> currentActivePanels = new ArrayList<>();
    @Autowired
    private IndividualPanelsService individualPanelsService;
    @Autowired
    private ContigousPanelsService contigousPanelsService;
    @Autowired
    private MirrorPanelsService mirrorPanelsService;
    @Autowired
    private IdentifyService identifyService;

    @GetMapping("")
    public String getPanel(Model model) {
        try {
            currentActivePanels = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
            currentActivePanels.addAll(repositoryService.getPanelsWithStatus(PanelStatus.INACTIVE));
            currentActivePanels = repositoryService.getPanels();
            currentActivePanels.removeIf(currentActivePanel -> currentActivePanel.getStatus().equals(PanelStatus.UNAVAILABLE));
            for (Panel currentActivePanel : currentActivePanels) {
                logger.info("\n\npanel : " + currentActivePanel.getName() );
                logger.info("|| brightness : " + currentActivePanel.getBrightness() );
                logger.info("|| cool-ness  : " + currentActivePanel.getBc() );
                logger.info("|| warm-ness  : " + currentActivePanel.getBw() );
            }
            model.addAttribute("panelList", currentActivePanels);
            model.addAttribute("setting", repositoryService.getSetting());
            model.addAttribute("output", repositoryService.getSetting().getP_output());
        } catch (Exception e) {
            System.out.println("getPanel ERROR : " + e);
            model.addAttribute("message", e.getMessage());
        }
        model.addAttribute("panelCreationRequest", new PanelCreationRequest());
        return "user/panelSettings";
    }

    @PostMapping("/sliderData")
    public ResponseEntity sliderData(@RequestParam("value") int value,
                                     @RequestParam("states") String statesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {
            });

            if (currentActivePanels == null || currentActivePanels.isEmpty()) {
                currentActivePanels = repositoryService.getPanels();
                currentActivePanels.removeIf(currentActivePanel -> currentActivePanel.getStatus().equals(PanelStatus.UNAVAILABLE));
            }

            for (int i = 0; i < states.size(); i++) {
                if (states.get(i)) {
                    brightnessService.setSingleBrightness(currentActivePanels.get(i).getId(), value);
                }
            }
            return ResponseEntity.ok("done");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/sliderDataWarm")
    public ResponseEntity sliderDataWarm(@RequestParam("value") int value,
                                     @RequestParam("states") String statesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {
            });

            if (currentActivePanels == null || currentActivePanels.isEmpty()) {
                currentActivePanels = repositoryService.getPanels();
                currentActivePanels.removeIf(currentActivePanel -> currentActivePanel.getStatus().equals(PanelStatus.UNAVAILABLE));
            }

            for (int i = 0; i < states.size(); i++) {
                if (states.get(i)) {
                    brightnessService.setSingleWarm(currentActivePanels.get(i).getId(), value);
                }
            }
            return ResponseEntity.ok("done");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/sliderDataCool")
    public ResponseEntity sliderDataCool(@RequestParam("value") int value,
                                     @RequestParam("states") String statesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {
            });

            if (currentActivePanels == null || currentActivePanels.isEmpty()) {
                currentActivePanels = repositoryService.getPanels();
                currentActivePanels.removeIf(currentActivePanel -> currentActivePanel.getStatus().equals(PanelStatus.UNAVAILABLE));
            }

            for (int i = 0; i < states.size(); i++) {
                if (states.get(i)) {
                    brightnessService.setSingleCool(currentActivePanels.get(i).getId(), value);
                }
            }
            return ResponseEntity.ok("done");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update-panel-connection")
    public ResponseEntity<?> updatePanelConnection(@RequestBody List<Boolean> states) {
        for (int i = 0; i < states.size(); i++) {
            Boolean state = states.get(i);
            currentActivePanels.get(i).setStatus(state ? PanelStatus.ACTIVE : PanelStatus.INACTIVE);
            repositoryService.updatePanel(currentActivePanels.get(i));
        }
        return ResponseEntity.ok("Checkbox states updated successfully.");
    }

    @PostMapping("/update-panel-output")
    public ResponseEntity<?> updatePanelOutput(@RequestBody String type) {
        try {
            repositoryService.updateSettingOutput(DisplayType.valueOf(type));
            return ResponseEntity.ok("Checkbox states updated successfully.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/identify")
    public ResponseEntity<Map<String, String>> identify() {
        individualPanelsService.pause();
        mirrorPanelsService.pause();
        contigousPanelsService.pause();
        Map<String, String> data = new HashMap<>();
        try {
            identifyService.startIdentify();
            data.put("Log", "Cleared!");
        } catch (InterruptedException | IOException e) {
            data.put("Log", "Error!");
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body(data);
    }
}