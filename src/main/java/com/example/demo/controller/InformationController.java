package com.example.demo.controller;

import com.example.demo.model.Information;
import com.example.demo.model.Profile;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.response.PaginatedInformationResponse;
import com.example.demo.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/information")
public class InformationController {
    private final RepositoryService repositoryService;
    private LibraryController libraryController;

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
            List<String> profileIds = new ArrayList<>();
            for (Profile profile : repositoryService.getProfile()) {
                profileIds.add(String.valueOf(profile.getId()));
            }
            model.addAttribute("informations", information);
            model.addAttribute("profileIds", profileIds);
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

    //TODO NOT WORKING//
    @GetMapping("/create")
    public String createInformation(InformationCreationRequest informationCreationRequest, RedirectAttributes redirectAttributes) {
        try {
            ResponseEntity<Information> response = libraryController.createInformation(informationCreationRequest);
            System.out.println(response.getStatusCode());
            redirectAttributes.addFlashAttribute("message", "The Information has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:";
    }

    @PostMapping("/update")
    public String updateInformation(InformationCreationRequest informationCreationRequest, RedirectAttributes redirectAttributes) {
        try {
            ResponseEntity<Information> response = libraryController.updateInformation(informationCreationRequest.getId(), informationCreationRequest);
            System.out.println(response.getStatusCode());
            redirectAttributes.addFlashAttribute("message", "The Information has been updated successfully!");
        } catch (Exception e) {
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
