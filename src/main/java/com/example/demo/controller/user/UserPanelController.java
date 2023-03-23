package com.example.demo.controller.user;

import com.example.demo.model.DisplayType;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.model.setting.PanelConfig;
import com.example.demo.model.setting.Setting;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.brightness.BrightnessService;
import com.example.demo.service.contigous.ContigousPanelsService;
import com.example.demo.service.individual.IndividualPanelsService;
import com.example.demo.service.mirror.MirrorPanelsService;
import com.example.demo.service.settings.IdentifyService;
import com.example.demo.service.settings.SettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

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

    @GetMapping("")
    public String getPanel(Model model) {
        try {
            settingService.setupActiveSettingFromActivePanel();
            customSetting = settingService.copyActivePanelToCustom();
            customSetting.getPanel_configs().removeIf(panelConfig -> panelConfig.getStatus().equals(PanelStatus.UNAVAILABLE));
            Setting activeSetting = repositoryService.getActiveSetting();
            activeSetting.getPanel_configs().removeIf(panelConfig -> panelConfig.getStatus().equals(PanelStatus.UNAVAILABLE));
            model.addAttribute("panelList", customSetting.getPanel_configs());
            model.addAttribute("activeSetting", activeSetting);
            model.addAttribute("settingList", repositoryService.getPresetSettings());
        } catch (Exception e) {
            System.out.println("getPanel ERROR : " + e);
            model.addAttribute("message", e.getMessage());
        }
        model.addAttribute("panelCreationRequest", new PanelCreationRequest());
        return "user/panelSettings";
    }

    @PostMapping("/sliderData")
    private ResponseEntity sliderData(@RequestParam("value") int value,
                                     @RequestParam("states") String statesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {});
            logger.info("value : " + value);
            states.forEach(state -> logger.info("state : " + state));

            Long[] respList = new Long[states.size()];
            boolean needUpdate = false;
            for (int i = 0; i < states.size(); i++) {
                if (states.get(i)) {
                    needUpdate = true;
                    Long panelId = customSetting.getPanel_configs().get(i).getId();
                    respList[i] = panelId;
                    customSetting.getPanel_configs().get(i).setBrightness(value);
                }
            }
            if(needUpdate){
                settingService.updateCustom(customSetting);
            }
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
                    logger.info("STATE : " + states.get(i));
                    logger.info("panelId : " + panelId);
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
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {});
            Long[] respList =  new Long[states.size()];
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
        try{
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
}