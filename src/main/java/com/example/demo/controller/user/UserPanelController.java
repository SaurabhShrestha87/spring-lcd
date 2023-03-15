package com.example.demo.controller.user;

import com.example.demo.controller.LibraryController;
import com.example.demo.model.DisplayType;
import com.example.demo.model.Information;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.model.response.PaginatedPanelResponse;
import com.example.demo.repository.PanelRepository;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.brightness.BrightnessService;
import com.example.demo.service.individual.IndividualPanelsService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.util.Console;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/user/panel")
public class UserPanelController {
    private static final Logger logger = LoggerFactory.getLogger(UserPanelController.class);
    @Autowired
    private final RepositoryService repositoryService;
    @Autowired
    private final PanelRepository panelRepository;
    @Autowired
    private final IndividualPanelsService individualPanelsService;
    @Autowired
    private final SerialCommunication serialCommunication;
    @Autowired
    private final BrightnessService brightnessService;
    public Console console = new Console();
    @Autowired
    private LibraryController libraryController;
    private List<Panel> currentActivePanels = new ArrayList<>();

    @PostConstruct
    public void init() {
        List<Panel> currentActivePanels = FileUtils.getPanelsList();
        List<Panel> dbAllPanels = repositoryService.getPanels();
        dbAllPanels.removeAll(currentActivePanels);
        for (Panel dbPanel : dbAllPanels) {
            dbPanel.setStatus(PanelStatus.DEACTIVATED);
            panelRepository.save(dbPanel);
        }
        for (Panel ipanel : currentActivePanels) {
            try {
                Panel dbPanel = panelRepository.findByName(ipanel.getName());
                if (dbPanel != null) {
                    BeanUtils.copyProperties(dbPanel, ipanel);
                } else {
                    ipanel.setId(0L);
                }
                ipanel.setStatus(PanelStatus.ACTIVE);
                panelRepository.save(ipanel);
            } catch (Exception e) {
                logger.error("Error :" + e);
            }
        }
    }

    @GetMapping("")
    public String getPanel(Model model, @RequestParam(defaultValue = "3") int size) {
        try {
            currentActivePanels = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
            model.addAttribute("panelList", currentActivePanels);
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            System.out.println("getPanel ERROR : " + e);
            model.addAttribute("message", e.getMessage());
        }
        model.addAttribute("panelCreationRequest", new PanelCreationRequest());
        return "user/panelSettings";
    }

    @PostMapping("/sliderData")
    public ResponseEntity sliderData(@RequestParam("value") int value,
                                     @RequestParam("percentage") String percentage,
                                     @RequestParam("states") String statesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Boolean> states = objectMapper.readValue(statesJson, new TypeReference<>() {});
            for (Boolean state : states) {
                logger.info("state : " + state);
            }

            if (currentActivePanels == null || currentActivePanels.isEmpty()) {
                currentActivePanels = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
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

    @PostMapping("/update-panel-connection")
    public ResponseEntity<?> updatePanelConnection(@RequestBody List<Boolean> states) {
        for (Boolean state : states) {
            logger.info("\nstate : " + state);
        }
        return ResponseEntity.ok("Checkbox states updated successfully.");
    }

    @PostMapping("/update-panel-output")
    public ResponseEntity<?> updatePanelOutput(@RequestBody String type) {
        logger.info("\nstate : " + type + "\n");
        return ResponseEntity.ok("Checkbox states updated successfully.");
    }
}