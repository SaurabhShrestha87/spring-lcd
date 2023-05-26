package com.example.demo.controller;

import com.example.demo.model.Information;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.model.response.PaginatedPanelResponse;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.individual.IndividualPanelsService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import com.pi4j.util.Console;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/panel")
public class PanelController {
    private static final Logger logger = LoggerFactory.getLogger(PanelController.class);
    @Autowired
    private final RepositoryService repositoryService;
    @Autowired
    private final IndividualPanelsService individualPanelsService;

    public Console console = new Console();
    @Autowired
    private LibraryController libraryController;

    /**
     Retrieves panels based on provided parameters and adds them to the model for rendering in the view.
     @param model The model object for adding attributes.
     @param keyword The keyword used for filtering the panels (optional).
     @param page The page number for pagination (default value: 1).
     @param size The number of items per page for pagination (default value: 3).
     @return The name of the view to render.
     */
    @GetMapping("")
    public String getPanel(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "3") int size) {
        // This method is used to retrieve panels and display them in the view.
        // It takes parameters such as keyword, page number, and size to support filtering and pagination.

        try {
            Pageable paging = PageRequest.of(page - 1, size);

            ResponseEntity<PaginatedPanelResponse> pagePanel;
            if (keyword == null || keyword.equalsIgnoreCase("")) {
                // If no keyword is provided or it is empty, retrieve all panels without filtering.
                pagePanel = libraryController.getPanel(paging);
            } else {
                // If a keyword is provided, retrieve panels with the filter applied.
                pagePanel = libraryController.getPanelWithFilter(keyword, paging);
                model.addAttribute("keyword", keyword);
            }

            // Retrieve the current active panels and the panels for the current page.
            List<Panel> currentActivePanels = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
            List<Panel> pagedPanels = pagePanel.getBody().getPanelList();

            // Add attributes to the model for rendering in the view.
            model.addAttribute("panelList", currentActivePanels); // All current active panels
            model.addAttribute("panels", pagedPanels); // Panels for the current page
            model.addAttribute("profiles", repositoryService.getProfile()); // All profiles
            model.addAttribute("currentPage", page); // Current page number
            model.addAttribute("totalItems", pagePanel.getBody().getNumberOfItems()); // Total number of items
            model.addAttribute("totalPages", pagePanel.getBody().getNumberOfPages()); // Total number of pages
            model.addAttribute("pageSize", size); // Number of items per page
        } catch (Exception e) {
            System.out.println("getPanel ERROR : " + e);
            model.addAttribute("message", e.getMessage());
        }

        model.addAttribute("panelCreationRequest", new PanelCreationRequest()); // Panel creation request
        return "panel/panel"; // Return the name of the view to render
    }

    @GetMapping("/fetch/{id}")
    @ResponseBody
    public Optional<Panel> fetch(@PathVariable("id") Long id) {
        // This method is used to fetch a panel with the given id.
        // It returns an Optional that may contain the panel if found, or an empty Optional if not found.
        return Optional.ofNullable(repositoryService.getPanel(id));
    }

    /**
     Updates a panel based on the provided panelCreationRequest and redirects to the default URL.
     @param panelCreationRequest The request object containing the updated panel details.
     @param redirectAttributes The redirect attributes for adding flash messages.
     @return The name of the view to redirect to.
     */
    @PostMapping("/update")
    public String updatePanel(PanelCreationRequest panelCreationRequest, RedirectAttributes redirectAttributes) {
        // This method is used to update a panel based on the provided panelCreationRequest.

        try {
            ResponseEntity<Panel> response = libraryController.updatePanel(panelCreationRequest.getId(), panelCreationRequest);
            // Invoke the library controller to update the panel and get the response.

            console.println(" ==>> Panel has been updated. Panel id: " + response.getBody().getId());
            // Print a message indicating that the panel has been updated.

            redirectAttributes.addFlashAttribute("message", "The Panel has been updated successfully!");
            // Add a flash attribute for the success message to be displayed after redirection.
        } catch (Exception e) {
            console.println(" ==>> Panel update failed. ERROR : " + e);
            // Print an error message if the panel update fails.

            redirectAttributes.addFlashAttribute("message", e.getMessage());
            // Add a flash attribute for the error message to be displayed after redirection.
        }

        return "redirect:";
        // Redirect to the default URL after the panel update.
    }

    /**
     Deletes a panel with the given ID and redirects to the parent directory.
     @param id The ID of the panel to be deleted.
     @param model The model object for adding attributes.
     @param redirectAttributes The redirect attributes for adding flash messages.
     @return The name of the view to redirect to.
     */
    @GetMapping("/delete/{id}")
    public String deletePanel(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        // This method is used to delete a panel with the given id.
        try {
            repositoryService.deletePanel(id);
            // Invoke the repository service to delete the panel based on the id.
            redirectAttributes.addFlashAttribute("message", "The Panel with id=" + id + " has been deleted successfully!");
            // Add a flash attribute for the success message to be displayed after redirection.
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            // Add a flash attribute for the error message to be displayed after redirection.
        }
        return "redirect:../";
        // Redirect to the parent directory after the panel deletion.
    }

    /**
     Handles file upload for a specific panel.
     @param file The uploaded file.
     @param panel The ID of the panel.
     @return The ResponseEntity with the response message.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("panel") String panel) {
        // This method is used to handle file upload for a specific panel.
        console.clearScreen();
        console.title("<-- handleFileUpload -->", "STARTED");
        console.println("\n[handleFileUpload API TRIGGERED]");
        String fileName = file.getOriginalFilename();
        String filePath = FileUtils.createFileDir(fileName);
        Panel panel1 = repositoryService.getPanel(Long.parseLong(panel));
        // Retrieve the panel information from the repository based on the given panel id.
        try {
            if (OSValidator.isWindows()) {
                file.transferTo(new File("D:\\upload\\" + fileName));
                // Transfer the uploaded file to the Windows file path.
            } else {
                file.transferTo(new File(filePath));
                // Transfer the uploaded file to the specified file path.
            }
        } catch (Exception e) {
            console.println("FileUpload Error " + e);
            // Print an error message if there's an exception during file upload.
        }
        Information info = new Information(0L, fileName, FileUtils.getFileType(fileName), filePath, null, "10", null);
        // Create an Information object with the file details.
        // String execute = individualPanelsService.execute(info, serialCommunication.getIndexFromDevice(panel1.getDevice()));
        // TODO: Uncomment and add the necessary logic for executing the information on the panel.
        return ResponseEntity.ok("TO BE CHANGED!");
        // Return a ResponseEntity with a placeholder message. Modify it as per the actual response requirements.
    }

    @GetMapping("/clearScreen")
    @ResponseBody
    public ResponseEntity<String> clearPanel() {
        try {
            // Call the clearAllScreens method from the individualPanelsService to clear the screens
            individualPanelsService.clearAllScreens();
            // Return a successful response with the message "Panel Reset completed"
            return ResponseEntity.ok("Panel Reset completed");
        } catch (Exception e) {
            // If an exception occurs, print the error message to the console
            System.out.println("clearPanel message" + e.getMessage());
            // Return a response indicating an error occurred during panel reset
            return ResponseEntity.ok("Panel Reset Error");
        }
    }
}

