package com.example.demo.controller.user;

import com.example.demo.model.DisplayType;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.model.setting.PanelConfig;
import com.example.demo.model.setting.Setting;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.contigous.ContigousPanelsService;
import com.example.demo.service.individual.IndividualPanelsService;
import com.example.demo.service.mirror.MirrorPanelsService;
import com.example.demo.service.settings.IdentifyService;
import com.example.demo.service.settings.SettingService;
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

import java.io.IOException;
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
    private final SettingService settingService;
    @Autowired
    private IndividualPanelsService individualPanelsService;
    @Autowired
    private ContigousPanelsService contigousPanelsService;
    @Autowired
    private MirrorPanelsService mirrorPanelsService;
    @Autowired
    private IdentifyService identifyService;
    Setting customSetting = null;
    /**
     * Retrieves panel data for the specified endpoint.
     * Handles a GET request to "/".
     *
     * @param model the model object to add attributes for the view
     * @return the name of the view template to render
     */
    @GetMapping("")
    public String getPanel(Model model) {
        try {
            // Setup the active setting based on the active panel
            settingService.setupActiveSettingFromActivePanel();
            // Create a custom setting by copying the active panel
            customSetting = settingService.copyActivePanelToCustom();
            // Remove panel configurations with the status "UNAVAILABLE" from the custom setting
            customSetting.getPanel_configs().removeIf(panelConfig -> panelConfig.getStatus().equals(PanelStatus.UNAVAILABLE));
            // Get the active setting from the repository
            Setting activeSetting = repositoryService.getActiveSetting();
            // Remove panel configurations with the status "UNAVAILABLE" from the active setting
            activeSetting.getPanel_configs().removeIf(panelConfig -> panelConfig.getStatus().equals(PanelStatus.UNAVAILABLE));
            // Add panelList, activeSetting, and settingList attributes to the model
            model.addAttribute("panelList", customSetting.getPanel_configs());
            model.addAttribute("activeSetting", activeSetting);
            model.addAttribute("settingList", repositoryService.getPresetSettings());
        } catch (Exception e) {
            System.out.println("getPanel ERROR : " + e);
            model.addAttribute("message", e.getMessage());
        }
        // Add a panelCreationRequest attribute to the model
        model.addAttribute("panelCreationRequest", new PanelCreationRequest());
        // Return the name of the view template to render
        return "user/panelSettings";
    }
    /**
     * Handles slider data received from a POST request to "/sliderData".
     *
     * @param value       the slider value parameter received from the request
     * @param statesJson  the JSON string representing the states of the panels
     * @return a ResponseEntity with the response message
     */
    @PostMapping("/sliderData")
    private ResponseEntity sliderData(@RequestParam("value") int value,
                                      @RequestParam("states") String statesJson) {
        try {
            // Initialize an ObjectMapper to parse the JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            // Deserialize the JSON string to a list of Boolean states
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {
            });
            // Create an array to store the panel IDs that need to be updated
            Long[] respList = new Long[states.size()];
            // Flag to track if an update is needed
            boolean needUpdate = false;
            // Iterate over the states list
            for (int i = 0; i < states.size(); i++) {
                if (states.get(i)) {
                    needUpdate = true;
                    // Get the panel ID for the current index
                    Long panelId = customSetting.getPanel_configs().get(i).getId();
                    // Store the panel ID in the response list
                    respList[i] = panelId;
                    // Set the brightness value for the panel configuration
                    customSetting.getPanel_configs().get(i).setBrightness(value);
                }
            }
            // If an update is needed, update the custom setting
            if (needUpdate) {
                settingService.updateCustom(customSetting);
            }
            // Return a ResponseEntity with the response list
            return ResponseEntity.ok(respList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/sliderDataWarm")
    private ResponseEntity sliderDataWarm(@RequestParam("value") int value,
                                          @RequestParam("states") String statesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {
            });
            Long[] respList = new Long[states.size()];
            for (int i = 0; i < states.size(); i++) {
                if (states.get(i)) {
                    Long panelId = customSetting.getPanel_configs().get(i).getId();
                    respList[i] = panelId;
                    customSetting.getPanel_configs().get(i).setBw(value);
                }
            }
            settingService.updateCustom(customSetting);
            return ResponseEntity.ok(respList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/sliderDataCool")
    private ResponseEntity sliderDataCool(@RequestParam("value") int value,
                                          @RequestParam("states") String statesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {
            });
            Long[] respList = new Long[states.size()];
            for (int i = 0; i < states.size(); i++) {
                if (states.get(i)) {
                    Long panelId = customSetting.getPanel_configs().get(i).getId();
                    respList[i] = panelId;
                    customSetting.getPanel_configs().get(i).setBc(value);
                }
            }
            settingService.updateCustom(customSetting);
            return ResponseEntity.ok(respList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update-panel-connection")
    private ResponseEntity<?> updatePanelConnection(@RequestBody List<Boolean> states) {
        try {
            for (int i = 0; i < states.size(); i++) {
                Boolean state = states.get(i);
                customSetting.getPanel_configs().get(i).setStatus(state ? PanelStatus.ACTIVE : PanelStatus.INACTIVE);
            }
            settingService.updateCustom(customSetting);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Checkbox states updated successfully.");
    }

    @PostMapping("/update-panel-output")
    private ResponseEntity<?> updatePanelOutput(@RequestBody String type) {
        try {
            customSetting.setP_output(DisplayType.valueOf(type));
            settingService.updateCustom(customSetting);
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

    @PostMapping("/save-custom-to-setting")
    public ResponseEntity<?> saveCustomToSetting(@RequestParam("value") int settingId) {
        try {
            settingService.saveCustomToSettingWithId(customSetting, settingId);
            return ResponseEntity.ok("new setting updated successfully.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/load-setting")
    public ResponseEntity<?> loadSetting(@RequestParam("value") int settingId) {
        try {
            settingService.setSelected((long) settingId);
            return ResponseEntity.ok("new setting loaded successfully.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/change-sn")
    public ResponseEntity<?> changeSn(@RequestParam("panelId") int panelId, @RequestParam("value") int sn) {
        try {
            logger.info("panelId " + panelId);
            logger.info("sn " + sn);
            PanelConfig panel1 = customSetting.getPanel_configs()
                    .stream()
                    .filter(panelConfig -> panelConfig.getId() == (panelId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("PanelConfig not found."));
            PanelConfig panel2 = customSetting.getPanel_configs()
                    .stream()
                    .filter(panelConfig -> panelConfig.getSn() == (sn) && panelConfig.getSetting().equals(panel1.getSetting()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("PanelConfig not found."));
            int panel1Sn = panel1.getSn();
            int panel2Sn = panel2.getSn();
            if (panel1.getSn() == sn) {
                logger.error("SAME sn as og!");
                return ResponseEntity.ok("new setting loaded successfully.");
            }
            panel1.setSn(panel2Sn);
            panel2.setSn(panel1Sn);
            for (PanelConfig panelConfig : customSetting.getPanel_configs()) {
                logger.info("\nNAME : " + panelConfig.getName() + " SN : " + panelConfig.getSn());
            }

            settingService.updateCustom(customSetting);
            // Do something with panelId and settingId
            return ResponseEntity.ok("new setting loaded successfully.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}