package com.example.demo.controller;

import com.example.demo.model.Lend;
import com.example.demo.model.Panel;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.model.response.PaginatedLendResponse;
import com.example.demo.service.LedService;
import com.example.demo.service.RepositoryService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
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
@RequestMapping(value = "/lend")
public class LendController {
    private final RepositoryService repositoryService;
    LedService ledService = new LedService();
    List<Panel> panelList = new ArrayList<>();
    private LibraryController libraryController;

    @GetMapping("")
    public String getLend(Model model,
                          @RequestParam(required = false) Long panelId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "3") int size) {
        try {
            libraryController = new LibraryController(repositoryService);
            List<Lend> lendList;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedLendResponse> pageLend;

            if (panelId == null) {
                pageLend = libraryController.getLend(paging);
            } else {
                pageLend = libraryController.getLend(panelId, paging);
                model.addAttribute("keyword", panelId);
            }
            lendList = pageLend.getBody().getLendList();
            for (Lend lend : lendList) {
                System.out.println(lend.toString());
            }
            model.addAttribute("lends", lendList);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalItems", pageLend.getBody().getNumberOfItems());
            model.addAttribute("totalPages", pageLend.getBody().getNumberOfPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }
        return "lend";
    }

    @GetMapping("/lend/delete/{id}")
    public String deleteLend(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            repositoryService.deleteLend(id);
            redirectAttributes.addFlashAttribute("message", "The Profile with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/lend";
    }

}
