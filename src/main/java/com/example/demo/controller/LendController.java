package com.example.demo.controller;

import com.example.demo.model.DisplayType;
import com.example.demo.model.Lend;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.request.LendCreationRequest;
import com.example.demo.model.request.PanelSelectionDto;
import com.example.demo.model.request.ProfileLendRequest;
import com.example.demo.model.response.PaginatedLendResponse;
import com.example.demo.repository.PanelRepository;
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
@RequestMapping(value = "/lend")
public class LendController {
    private final RepositoryService repositoryService;
    private LibraryController libraryController;
    private final PanelRepository panelRepository;

    //TODO show lends later with navigation for contigous/duplicate/extend
//    @GetMapping("")
    public String getLend(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "3") int size) {
        try {
            List<Lend> lendList;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedLendResponse> pageLend;
            if (keyword == null || keyword.equalsIgnoreCase("")) {
                pageLend = libraryController.getLend(paging);
            } else {
//                TODO maybe filter lend based on panel ids after panel crud is done...
//                pageLend = libraryController.filterLendWithPanelId(keyword, paging);
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
        model.addAttribute("informationCreationRequest", new InformationCreationRequest());
        return "lend/lend";
    }

    @PostMapping("/create")
    public String createLend(ProfileLendRequest profileLendRequest, RedirectAttributes redirectAttributes) {
        try {
            ResponseEntity<List<String>> response = libraryController.lendAProfile(profileLendRequest);
            System.out.println("createLend" + response.getStatusCode());
            redirectAttributes.addFlashAttribute("message", "The Lend has been saved successfully!");
        } catch (Exception e) {
            System.out.println("createLend ERROR" + e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:";
    }

    @PostMapping("/update")
    public String updateLend(LendCreationRequest lendCreationRequest, RedirectAttributes redirectAttributes) {
//        try {
//            ResponseEntity<Lend> response = libraryController.updateLend(lendCreationRequest.getId(), lendCreationRequest);
//            logger.info("Lend updated!: ID : " + response.getBody().getId());
//            redirectAttributes.addFlashAttribute("message", "The Lend has been updated successfully!");
//        } catch (Exception e) {
//            logger.info("Lend not updated! ERROR : " + e);
//            redirectAttributes.addFlashAttribute("message", e.getMessage());
//        }
        return "redirect:";
    }

//    @GetMapping("/fetch/{id}")
//    @ResponseBody
//    public Optional<LendCreationRequest> fetch(@PathVariable("id") Long id) {
//        logger.info("Lend has been fetched. Lend id: " + id);
//        Lend lend = repositoryService.getLend(id);
//        LendCreationRequest request = new LendCreationRequest();
//        request.setId(lend.getId());
//        request.setName(lend.getName());
//        request.setDate(lend.getDateAsString());
//        return Optional.ofNullable(request);
//    }

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

    //TODO show lends later with navigation for contigous/duplicate/extend (THIS GETS REMOVED AND UNCOMMENT BELOW LINE)
    @GetMapping("")
//    @GetMapping("/panelSelection")
    public String showCreateForm(Model model) {
        PanelSelectionDto panelSelectionDto = new PanelSelectionDto(new ArrayList<>());
        panelSelectionDto.setDisplayType(DisplayType.INDIVIDUAL);
        model.addAttribute("panels", repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE));
        model.addAttribute("panelSelection", panelSelectionDto);
        model.addAttribute("profileLendRequest", new ProfileLendRequest());
        return "lend/lendPanel";
    }

    @PostMapping(path = "/setPanel")
    public String addPanelOrSmn(@ModelAttribute ProfileLendRequest profileLendRequest, @ModelAttribute PanelSelectionDto panelSelection, Model model, RedirectAttributes redirectAttributes) {
        try {
            System.out.println(panelSelection.getDisplayType());
            System.out.println("This RAN!");
            StringBuilder ids = new StringBuilder();
            for (Panel panel : panelSelection.getPanelList()) {
                System.out.println("This RAN! +1 ");
                ids.append(", ").append(panel.getId());
            }
            redirectAttributes.addFlashAttribute("message", "The Panel with id=" + ids + " has fetched successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        //logic, etc.
        PanelSelectionDto panelSelectionDto = new PanelSelectionDto(new ArrayList<>());
        panelSelectionDto.setDisplayType(DisplayType.INDIVIDUAL);
        model.addAttribute("panels", repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE));
        model.addAttribute("panelSelection", panelSelectionDto);
        model.addAttribute("profileLendRequest", new ProfileLendRequest());
        return "lend/lendPanel";
    }
}
