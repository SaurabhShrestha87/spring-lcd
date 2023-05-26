package com.example.demo.controller;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.SerialCommunication;
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
@RequestMapping(value = "/home")
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
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
    @Autowired
    private SerialCommunication serialCommunication;
    private boolean inUse;

    /**
     * Retrieves active panels and prepares the model for the home view.
     * Handles a GET request to the root URL ("/").
     *
     * @param model the model object used to pass data to the view
     * @return the name of the home view template
     */
    @GetMapping("")
    public String getHome(Model model) {
        // Retrieve a list of active panels from the repository service
        List<Panel> list = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
        // Add the list of panels to the model
        model.addAttribute("panels", list);
        // Return the name of the home view template
        return "home/home";
    }

    /**
     * Receives the toggle state and performs actions based on the state.
     * Handles a POST request to "/togglePanel".
     *
     * @param toggleState the toggle state parameter received from the request
     * @return a ResponseEntity with the response message
     */
    @PostMapping("/togglePanel")
    public ResponseEntity receiveToggleState(@RequestParam("toggleState") boolean toggleState) {
        // Perform actions based on the toggle state
        if (toggleState) {
            // Stop mirror and contiguous panels services and start individual panels service
            mirrorPanelsService.stop();
            contigousPanelsService.stop();
            individualPanelsService.start();
        } else {
            // Pause individual panels service
            individualPanelsService.pause();
        }
        // Return a ResponseEntity with the response message
        return ResponseEntity.ok("hello!");
    }

    /**
     * Toggles the Contiguous Panels service based on the toggle state.
     * Handles a POST request to "/togglePanelContiguous".
     *
     * @param toggleState the toggle state parameter received from the request
     * @return a ResponseEntity with the response message
     */
    @PostMapping("/togglePanelContiguous")
    public ResponseEntity togglePanelContiguous(@RequestParam("toggleState") boolean toggleState) {
        // Perform actions based on the toggle state
        if (toggleState) {
            // Stop mirror and individual panels services and start contiguous panels service
            mirrorPanelsService.stop();
            individualPanelsService.stop();
            contigousPanelsService.start();
        } else {
            // Pause contiguous panels service
            contigousPanelsService.pause();
        }
        // Return a ResponseEntity with the response message
        return ResponseEntity.ok("hello!");
    }

    /**
     * Toggles the Mirror Panels service based on the toggle state.
     * Handles a POST request to "/togglePanelMirror".
     *
     * @param toggleState the toggle state parameter received from the request
     * @return a ResponseEntity with the response message
     */
    @PostMapping("/togglePanelMirror")
    public ResponseEntity togglePanelMirror(@RequestParam("toggleState") boolean toggleState) {
        // Perform actions based on the toggle state
        if (toggleState) {
            // Start mirror panels service and stop individual and contiguous panels services
            mirrorPanelsService.start();
            individualPanelsService.stop();
            contigousPanelsService.stop();
        } else {
            // Pause mirror panels service
            mirrorPanelsService.pause();
        }
        // Return a ResponseEntity with the response message
        return ResponseEntity.ok("done");
    }

    /**
     * Resets the application by stopping services, clearing screens, and resetting serial communication.
     * Handles a GET request to "/reset".
     *
     * @return a ResponseEntity with the response data
     */
    @GetMapping("/reset")
    public ResponseEntity<Map<String, String>> reset() {
        // Create a map to store the response data
        Map<String, String> data = new HashMap<>();

        // Stop the contiguous panels service and clear all screens
        contigousPanelsService.stop();
        contigousPanelsService.clearAllScreens();

        // Stop the mirror panels service and clear all screens
        mirrorPanelsService.stop();
        mirrorPanelsService.clearAllScreens();

        // Stop the individual panels service and clear all screens
        individualPanelsService.stop();
        individualPanelsService.clearAllScreens();

        // Reset the serial communication
        serialCommunication.resetSerial();

        // Add the log message to the response data
        data.put("Log", "Cleared!");

        // Return a ResponseEntity with the response data
        return ResponseEntity.ok().body(data);
    }
    /**
     * Retrieves data for the specified endpoint.
     * Handles a GET request to "/getData".
     *
     * @return a ResponseEntity with the response data
     */
    @GetMapping("/getData")
    public ResponseEntity<Map<String, String>> getData() {
        // Create a map to store the response data
        Map<String, String> data = new HashMap<>();

        // Add the initial log message to the response data
        data.put("Log", "");

        // Return a ResponseEntity with the response data
        return ResponseEntity.ok().body(data);
    }

    /**
     * Retrieves logs for the specified endpoint.
     * Handles a GET request to "/getLogs".
     *
     * @return a ResponseEntity with the logs data
     */
    @GetMapping("/getLogs")
    public ResponseEntity<Map<String, String>> getLogs() {
        // Create a map to store the logs data
        Map<String, String> logs = new HashMap<>();

        // Add the initial log message to the logs data
        logs.put("Log", "");

        // Return a ResponseEntity with the logs data
        return ResponseEntity.ok().body(logs);
    }

    /**
     * Handles slider data received from a POST request to "/sliderData".
     *
     * @param value      the slider value parameter received from the request
     * @param percentage the slider percentage parameter received from the request
     * @return a ResponseEntity with the response message
     */
    @PostMapping("/sliderData")
    public ResponseEntity sliderData(@RequestParam("value") int value, @RequestParam("percentage") String percentage) {
        // Set the brightness value using the brightness service
        brightnessService.setBrightness(value);

        // Return a ResponseEntity with the response message
        return ResponseEntity.ok("done");
    }

    /**
     * Handles single slider data received from a POST request to "/singleSliderData".
     *
     * @param value      the slider value parameter received from the request
     * @param percentage the slider percentage parameter received from the request
     * @param panelId    the panel ID parameter received from the request
     * @return a ResponseEntity with the response message
     */
    @PostMapping("/singleSliderData")
    public ResponseEntity singleSliderData(@RequestParam("value") int value, @RequestParam("percentage") String percentage,
                                           @RequestParam("panelId") Long panelId) {
        // Set the "inUse" flag to indicate that the slider is being used
        inUse = true;

        // Set the brightness value for the specified panel ID using the brightness service
        brightnessService.setSingleBrightness(panelId, value);

        // Set the "inUse" flag back to false once the operation is done
        inUse = false;

        // Return a ResponseEntity with the response message
        return ResponseEntity.ok("done");
    }
}
