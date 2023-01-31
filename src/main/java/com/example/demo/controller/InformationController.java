package com.example.demo.controller;

import com.example.demo.model.Information;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.response.PaginatedInformationResponse;
import com.example.demo.service.RepositoryService;
import com.example.demo.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/information")
public class InformationController {
    private final RepositoryService repositoryService;
    private LibraryController libraryController;

    private static final Logger logger = LoggerFactory.getLogger(InformationController.class);

    @GetMapping("")
    public String getInformation(Model model,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "3") int size) {
        try {
            libraryController = new LibraryController(this.repositoryService);
            List<Information> information;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedInformationResponse> pageInformation;

            if (keyword == null || keyword.equalsIgnoreCase("")) {
                pageInformation = libraryController.getInformation(paging);
            } else {
                pageInformation = libraryController.getInformationWithFilter(keyword, paging);
                model.addAttribute("keyword", keyword);
            }
            information = pageInformation.getBody().getInformationList();

            model.addAttribute("informations", information);
            model.addAttribute("profiles", repositoryService.getProfile());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalItems", pageInformation.getBody().getNumberOfItems());
            model.addAttribute("totalPages", pageInformation.getBody().getNumberOfPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            System.out.println("getInformation ERROR : " + e);
            model.addAttribute("message", e.getMessage());
        }
        model.addAttribute("informationCreationRequest", new InformationCreationRequest());
        return "information/information";
    }

    @PostMapping("/create")
    public String createInformation(InformationCreationRequest informationCreationRequest, RedirectAttributes redirectAttributes) {
        System.out.println("THIS RAN");
        System.out.println("THIS RAN" + informationCreationRequest.getInfoType());
        try {
            informationCreationRequest.setFileURL(FileUtils.createFileDir(informationCreationRequest.getMultipartFile().getOriginalFilename()));
            System.out.println("createInformation " + informationCreationRequest);
            ResponseEntity<Information> response = libraryController.createInformation(informationCreationRequest);
            System.out.println(response.getStatusCode());
            redirectAttributes.addFlashAttribute("message", "The Information has been saved successfully!");
        } catch (Exception e) {
            System.out.println("createInformation " + e.getMessage());
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:";
    }

    @GetMapping("/fetch/{id}")
    @ResponseBody
    public Optional<Information> fetch(@PathVariable("id") Long id) {
        logger.info("Information has been fetched. Information id: " + id);
        return Optional.ofNullable(repositoryService.getInformation(id));
    }

    @PostMapping("/update")
    public String updateInformation(InformationCreationRequest informationCreationRequest, RedirectAttributes redirectAttributes) {
        logger.info("updateInformation: " + informationCreationRequest.toString());
        libraryController = new LibraryController(repositoryService);
        try {
            ResponseEntity<Information> response = libraryController.updateInformation(informationCreationRequest.getId(), informationCreationRequest);
            logger.info("Information has been updated. Information id: " + response.getBody().getId());
            redirectAttributes.addFlashAttribute("message", "The Information has been updated successfully!");
        } catch (Exception e) {
            logger.info("Information update failed. ERROR : " + e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:";
    }

    @GetMapping("/delete/{id}")
    public String deleteInformation(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            repositoryService.deleteInformation(id);
            redirectAttributes.addFlashAttribute("message", "The Information with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:../";
    }

}
