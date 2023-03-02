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

    @GetMapping("")
    public String getInformation(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "3") int size) {
        try {
            List<Information> information;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedInformationResponse> pageInformation;

            if (keyword == null || keyword.equalsIgnoreCase("")) {
                pageInformation = libraryController.getBaseInformation(paging);
            } else {
                pageInformation = libraryController.getBaseInformationWithFilter(keyword, paging);
                model.addAttribute("keyword", keyword);
            }
            information = pageInformation.getBody().getInformationList();

            model.addAttribute("informations", information);
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
        try {
            String filePath = FileUtils.createFileDir(informationCreationRequest.getMultipartFile().getOriginalFilename());
            informationCreationRequest.setFileURL(filePath);
            try {
                if (OSValidator.isWindows()) {
                    informationCreationRequest.getMultipartFile().transferTo(new File("D:\\upload\\" + informationCreationRequest.getMultipartFile().getOriginalFilename()));
                } else {
                    informationCreationRequest.getMultipartFile().transferTo(new File(filePath));
                }
            } catch (Exception e) {
                logger.error("FileUpload Error " + e);
            }
            ResponseEntity<Information> response = libraryController.createInformation(informationCreationRequest);
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
        try {
            ResponseEntity<Information> response = libraryController.updateInformation(informationCreationRequest.getId(), informationCreationRequest);
            logger.info("Information has been updated. Information id: " + response.getBody().getId());
            redirectAttributes.addFlashAttribute("message", "The Information has been updated successfully!");
        } catch (Exception e) {
            logger.info("Information update failed. ERROR : " + e);
            redirectAttributes.addFlashAttribute("message", "ERROR : " + e);
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
