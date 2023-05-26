package com.example.demo.controller;

import com.example.demo.model.Information;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.response.PaginatedInformationResponse;
import com.example.demo.service.RepositoryService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Controller
@CrossOrigin("*")
@NoArgsConstructor
@RequestMapping(value = "/information")
public class InformationController {
    private static final Logger logger = LoggerFactory.getLogger(InformationController.class);
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private LibraryController libraryController;

    /**
     Retrieves information based on provided parameters and adds it to the model for rendering in the view.
     @param model The model object for adding attributes.
     @param keyword The keyword used for filtering the information (optional).
     @param page The page number for pagination (default value: 1).
     @param size The number of items per page for pagination (default value: 3).
     @return The name of the view to render.
     */
    @GetMapping("")
    public String getInformation(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "3") int size) {
        try {
            List<Information> information;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedInformationResponse> pageInformation;

            // Check if a keyword is provided
            if (keyword == null || keyword.equalsIgnoreCase("")) {
                // Retrieve base information without filtering
                pageInformation = libraryController.getBaseInformation(paging);
            } else {
                // Retrieve base information with keyword filtering
                pageInformation = libraryController.getBaseInformationWithFilter(keyword, paging);
                model.addAttribute("keyword", keyword);
            }
            information = pageInformation.getBody().getInformationList();

            // Add retrieved information to the model for rendering in the view
            model.addAttribute("informations", information);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalItems", pageInformation.getBody().getNumberOfItems());
            model.addAttribute("totalPages", pageInformation.getBody().getNumberOfPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            // Handle any exceptions that occur and display an error message
            System.out.println("getInformation ERROR : " + e);
            model.addAttribute("message", e.getMessage());
        }

        // Add an empty InformationCreationRequest object to the model for creating new information
        model.addAttribute("informationCreationRequest", new InformationCreationRequest());

        // Return the name of the view to render (in this case, "information/information")
        return "information/information";
    }
    /**
     Creates a new information based on the provided informationCreationRequest and redirects to the main page.
     @param informationCreationRequest The request object containing the information details and uploaded file.
     @param redirectAttributes The redirect attributes for adding flash messages.
     @return The name of the view to redirect to.
     */

    @PostMapping("/create")
    public String createInformation(InformationCreationRequest informationCreationRequest, RedirectAttributes redirectAttributes) {
        try {
            // Create the file path for storing the uploaded file
            String filePath = FileUtils.createFileDir(informationCreationRequest.getMultipartFile().getOriginalFilename());
            informationCreationRequest.setFileURL(filePath);

            try {
                // Transfer the uploaded file to the appropriate location
                if (OSValidator.isWindows()) {
                    // For Windows, use a specific directory
                    informationCreationRequest.getMultipartFile().transferTo(new File("D:\\upload\\" + informationCreationRequest.getMultipartFile().getOriginalFilename()));
                } else {
                    // For other operating systems, use the generated file path
                    informationCreationRequest.getMultipartFile().transferTo(new File(filePath));
                }
            } catch (Exception e) {
                // Handle any exceptions that occur during file upload and log an error
                logger.error("FileUpload Error " + e);
            }

            // Send a request to create the information using the provided informationCreationRequest
            ResponseEntity<Information> response = libraryController.createInformation(informationCreationRequest);

            // Add a flash attribute to redirect and display a success message
            redirectAttributes.addFlashAttribute("message", "The Information has been saved successfully!");
        } catch (Exception e) {
            // Handle any exceptions that occur during the process and add an error message
            System.out.println("createInformation " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        // Redirect to the main page after creating the information
        return "redirect:";
    }


    @GetMapping("/fetch/{id}")
    @ResponseBody
    public Optional<Information> fetch(@PathVariable("id") Long id) {
        // Retrieve the Information entity with the specified ID using the repository service
        return Optional.ofNullable(repositoryService.getInformation(id));
    }

    /**
     Updates an existing information based on the provided informationCreationRequest and redirects to the main page.
     @param informationCreationRequest The request object containing the updated information details.
     @param redirectAttributes The redirect attributes for adding flash messages.
     @return The name of the view to redirect to.
     */
    @PostMapping("/update")
    public String updateInformation(InformationCreationRequest informationCreationRequest, RedirectAttributes redirectAttributes) {
        try {
            // Send a request to update the information with the provided ID and informationCreationRequest
            ResponseEntity<Information> response = libraryController.updateInformation(informationCreationRequest.getId(), informationCreationRequest);

            // Add a flash attribute to redirect and display a success message
            redirectAttributes.addFlashAttribute("message", "The Information has been updated successfully!");
        } catch (Exception e) {
            // Handle any exceptions that occur during the update process and log an error
            logger.error("Information update failed. Error: " + e);
            redirectAttributes.addFlashAttribute("message", "ERROR: " + e);
        }

        // Redirect to the main page after updating the information
        return "redirect:";
    }
    /**
     Deletes an information with the specified ID and redirects to the main page.
     @param id The ID of the information to be deleted.
     @param model The model object for adding attributes.
     @param redirectAttributes The redirect attributes for adding flash messages.
     @return The name of the view to redirect to.
     */
    @GetMapping("/delete/{id}")
    public String deleteInformation(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Delete the Information entity with the specified ID using the repository service
            repositoryService.deleteInformation(id);

            // Add a flash attribute to redirect and display a success message
            redirectAttributes.addFlashAttribute("message", "The Information with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            // Handle any exceptions that occur during the deletion process and add an error message
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        // Redirect to the main page after deleting the information
        return "redirect:../";
    }
}
