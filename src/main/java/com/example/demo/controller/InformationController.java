package com.example.demo.controller;

import com.example.demo.model.Information;
import com.example.demo.model.Lend;
import com.example.demo.model.Panel;
import com.example.demo.model.Profile;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.model.response.PaginatedInformationResponse;
import com.example.demo.model.response.PaginatedLendResponse;
import com.example.demo.service.LedService;
import com.example.demo.service.RepositoryService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import com.pi4j.io.serial.Serial;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/information")
public class InformationController {
    private final RepositoryService repositoryService;
    LedService ledService = new LedService();
    List<Panel> panelList = new ArrayList<>();
    private LibraryController libraryController;
    private Serial serial;

    @GetMapping("")
    public String getAll(Model model,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "3") int size) {
        try {
            libraryController = new LibraryController(repositoryService);
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
            for (Information information1 : information) {
                System.out.println(information1.toString());
            }
            model.addAttribute("informations", information);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalItems", pageInformation.getBody().getNumberOfItems());
            model.addAttribute("totalPages", pageInformation.getBody().getNumberOfPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }
        return "information/information";
    }

    //TODO NOT WORKING//
    @GetMapping("/new")
    public String addInformation(Model model) {
        List<String> profileIds = new ArrayList<>();
        for (Profile profile : repositoryService.getProfile()) {
            profileIds.add(String.valueOf(profile.getId()));
            System.out.println("PROFILE ID : " + profile.getId());
        }
        InformationCreationRequest informationCreationRequest = new InformationCreationRequest();
        model.addAttribute("informationCreationRequest", informationCreationRequest);
        model.addAttribute("profileIds", profileIds);
        model.addAttribute("pageTitle", "Create new Information");
        return "information/addInformation";
    }

    @PostMapping("/save")
    public String saveInformation(InformationCreationRequest informationCreationRequest) {
        informationCreationRequest.setFileURL(FileUtils.createFileDir(informationCreationRequest.getMultipartFile().getOriginalFilename()));
        System.out.println(informationCreationRequest.toString());
        try {
            repositoryService.createInformation(informationCreationRequest);
        } catch (Exception e) {
            System.out.println(e);
        }
        return "redirect:information/information";
    }

    @GetMapping("/delete/{id}")
    public String deleteInformation(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            repositoryService.deleteInformation(id);
            redirectAttributes.addFlashAttribute("message", "The Information with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:information/information";
    }

}
