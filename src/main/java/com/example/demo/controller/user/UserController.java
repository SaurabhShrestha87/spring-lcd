package com.example.demo.controller.user;

import com.example.demo.model.*;
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
import java.util.concurrent.CompletableFuture;

import static com.example.demo.model.ExtractionState.*;

@Controller
@NoArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private SerialCommunication serialCommunication;
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

    /**
     * Retrieves user data and prepares the model for the user view.
     * Handles a GET request to the root URL ("/").
     *
     * @param model the model object used to pass data to the view
     * @return the name of the user view template
     */
    @GetMapping("")
    public String getUser(Model model) {
        // Retrieve a list of active panels from the repository service
        List<Panel> list = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);

        // Determine if the extraction state of any panel service is running
        boolean isStopped = individualPanelsService.extractionState != RUNNING &&
                contigousPanelsService.extractionState != RUNNING &&
                mirrorPanelsService.extractionState != RUNNING;

        // Get the latest output type from the active setting in the repository service
        DisplayType latestOutput = repositoryService.getActiveSetting().getP_output();

        // Check if any panel service extraction state is not stopped and there is a current output
        if (individualPanelsService.extractionState != STOPPED ||
                contigousPanelsService.extractionState != STOPPED ||
                mirrorPanelsService.extractionState != STOPPED) {
            if (currentOutput != null) {
                latestOutput = currentOutput;
            }
        }

        // Process the latest output type
        switch (latestOutput) {
            case INDIVIDUAL -> {
                // Process INDIVIDUAL output type
                int panelCount = serialCommunication.getSize();
                for (int i = 0; i < panelCount; i++) {
                    // Retrieve running lends for each panel and add them to the model
                    List<Lend> runningLends = repositoryService.findAllByPanelIdAndStatus(
                            serialCommunication.panelIdFromIndex(i),
                            LendStatus.RUNNING,
                            DisplayType.INDIVIDUAL);
                    model.addAttribute("queue" + i, runningLends);
                }
                model.addAttribute("queueCount", panelCount);
            }
            case CONTIGUOUS -> {
                // Process CONTIGUOUS output type
                List<Lend> runningLends = repositoryService.findAllByTypeAndStatus(
                        DisplayType.CONTIGUOUS,
                        LendStatus.RUNNING);
                model.addAttribute("queueCount", 1);
                model.addAttribute("queue" + 1, runningLends);
            }
            case MIRROR -> {
                // Process MIRROR output type
                List<Lend> runningLends = repositoryService.findAllByTypeAndStatus(
                        DisplayType.MIRROR,
                        LendStatus.RUNNING);
                model.addAttribute("queueCount", 1);
                model.addAttribute("queue" + 1, runningLends);
            }
        }

        // Add attributes to the model
        model.addAttribute("output", latestOutput.name());
        model.addAttribute("panels", list);
        model.addAttribute("isStopped", isStopped);

        // Log the contents of the model
        logger.info("Model contents: {}", model.asMap());

        // Return the name of the user view template
        return "user/user";
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
        // Check if any panel service extraction state is not stopped and set the current output if stopped
        if (individualPanelsService.extractionState != STOPPED ||
                contigousPanelsService.extractionState != STOPPED ||
                mirrorPanelsService.extractionState != STOPPED) {
            // No action needed
        } else {
            currentOutput = repositoryService.getActiveSetting().getP_output();
        }

        // Create a CompletableFuture to run the code asynchronously
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // Perform actions based on the toggle state
            if (toggleState) {
                switch (currentOutput) {
                    case INDIVIDUAL -> {
                        // Start individual panels service and stop other services
                        mirrorPanelsService.stop();
                        contigousPanelsService.stop();
                        individualPanelsService.start();
                    }
                    case CONTIGUOUS -> {
                        // Start contiguous panels service and stop other services
                        mirrorPanelsService.stop();
                        contigousPanelsService.start();
                        individualPanelsService.stop();
                    }
                    case MIRROR -> {
                        // Start mirror panels service and stop other services
                        mirrorPanelsService.start();
                        contigousPanelsService.stop();
                        individualPanelsService.stop();
                    }
                }
            } else {
                // Pause all panel services
                mirrorPanelsService.pause();
                contigousPanelsService.pause();
                individualPanelsService.pause();
            }
        });

        // Return a ResponseEntity with the response message
        return ResponseEntity.ok("hello!");
    }

    /**
     * Resets the application by clearing screens, stopping services, and resetting serial communication.
     * Handles a GET request to "/reset".
     *
     * @return a ResponseEntity with the response data
     */
    @GetMapping("/reset")
    public ResponseEntity<Map<String, String>> reset() {
        // Create a map to store the response data
        Map<String, String> data = new HashMap<>();

        // Reset the current output to null
        currentOutput = null;

        // Stop and clear screens for the contiguous panels service
        contigousPanelsService.stop();
        contigousPanelsService.clearAllScreens();

        // Stop and clear screens for the mirror panels service
        mirrorPanelsService.stop();
        mirrorPanelsService.clearAllScreens();

        // Stop and clear screens for the individual panels service
        individualPanelsService.stop();
        individualPanelsService.clearAllScreens();

        // Reset the serial communication
        serialCommunication.resetSerial();

        // Add the log message to the response data
        data.put("Log", "Cleared!");

        // Return a ResponseEntity with the response data
        return ResponseEntity.ok().body(data);
    }
}
