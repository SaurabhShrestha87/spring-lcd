package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.model.request.LendCreationRequest;
import com.example.demo.model.request.PanelSelectionDto;
import com.example.demo.model.request.ProfileLendRequest;
import com.example.demo.model.response.PaginatedLendResponse;
import com.example.demo.repository.PanelRepository;
import com.example.demo.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/lend")
public class LendController {
    @Autowired
    private final RepositoryService repositoryService;
    @Autowired
    private final PanelRepository panelRepository;
    @Autowired
    private LibraryController libraryController;

    @GetMapping("")
    public String getLend(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "3") int size) {
        try {
            List<Lend> lendList;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedLendResponse> pageLend;
            if (keyword == null || keyword.equalsIgnoreCase("")) {
                pageLend = libraryController.getLend(paging);
            } else {
                pageLend = libraryController.getLend(paging);
                model.addAttribute("keyword", keyword);
            }
            lendList = pageLend.getBody().getLendList();
            model.addAttribute("lends", lendList);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalItems", pageLend.getBody().getNumberOfItems());
            model.addAttribute("totalPages", pageLend.getBody().getNumberOfPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }
        model.addAttribute("lendCreationRequest", new LendCreationRequest());
        PanelSelectionDto panelSelectionDto = new PanelSelectionDto(new ArrayList<>());
        panelSelectionDto.setDisplayType(DisplayType.INDIVIDUAL);
        model.addAttribute("panels", repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE));
        model.addAttribute("panelSelection", panelSelectionDto);
        model.addAttribute("profileLendRequest", new ProfileLendRequest());
        model.addAttribute("profiles", repositoryService.getProfile());
        return "lend/lend";
    }

    @GetMapping("/delete/{id}")
    public String deleteLend(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            repositoryService.deleteLend(id);
            redirectAttributes.addFlashAttribute("message", "The Lend with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:../";
    }

    @PostMapping(path = "/setPanel")
    public String setPanel(@ModelAttribute PanelSelectionDto panelSelection, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Panel> finalPanelList;
            if (panelSelection.getDisplayType().equals(DisplayType.INDIVIDUAL)) {
                finalPanelList = panelSelection.getPanelList();
            } else {
                finalPanelList = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
            }
            List<String> profileApprovedToLend = new ArrayList<>();
            for (Panel finalPanel : finalPanelList) {
                System.out.println("This RAN! finalPanelList");
                ProfileLendRequest profileLendRequest = new ProfileLendRequest();
                profileLendRequest.setPanelId(finalPanel.getId());
                profileLendRequest.setProfileIds(panelSelection.getProfileIds());
                profileLendRequest.setDisplayType(panelSelection.getDisplayType());
                profileApprovedToLend.addAll(repositoryService.lendAProfile(profileLendRequest));
                if (panelSelection.getDisplayType().equals(DisplayType.CONTIGUOUS) || panelSelection.getDisplayType().equals(DisplayType.DUPLICATE)) {
                    break;
                }
            }
            redirectAttributes.addFlashAttribute("message", "Lending Complete = " + profileApprovedToLend);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        //logic, etc.
        PanelSelectionDto panelSelectionDto = new PanelSelectionDto(new ArrayList<>());
        panelSelectionDto.setDisplayType(DisplayType.INDIVIDUAL);
        model.addAttribute("panels", repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE));
        model.addAttribute("panelSelection", panelSelectionDto);
        model.addAttribute("profileLendRequest", new ProfileLendRequest());
        model.addAttribute("profiles", repositoryService.getProfile());
        return "redirect:../";
    }

    @PostMapping("/toggleLend")
    @ResponseBody
    public String toggleLend(@RequestParam("id") long id, @RequestParam("toggleState") boolean toggleState) {
        Lend lend = repositoryService.getLend(id);
        Lend lendToUpdate = new Lend();
        BeanUtils.copyProperties(lend, lendToUpdate);
        lendToUpdate.setStatus(toggleState ? LendStatus.RUNNING : LendStatus.AVAILABLE);
        Lend updatedLend = repositoryService.updatelend(id, lendToUpdate);
        String resp = updatedLend.getStatus().toString();
        System.out.printf(resp);
        return resp;
    }
}
