package com.example.demo.controller;

import com.example.demo.model.Information;
import com.example.demo.model.Profile;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.request.ProfileCreationRequest;
import com.example.demo.model.response.PaginatedProfileResponse;
import com.example.demo.service.RepositoryService;
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
@RequestMapping(value = "/profile")
public class ProfileController {
    private final RepositoryService repositoryService;
    private final LibraryController libraryController;
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @GetMapping("")
    public String getProfile(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "3") int size) {
        try {
            List<Profile> profileList;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedProfileResponse> pageProfile;
            if (keyword == null || keyword.equalsIgnoreCase("")) {
                pageProfile = libraryController.getProfile(paging);
            } else {
                pageProfile = libraryController.getProfileWithFilter(keyword, paging);
                model.addAttribute("keyword", keyword);
            }
            profileList = pageProfile.getBody().getProfileList();
            model.addAttribute("profiles", profileList);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalItems", pageProfile.getBody().getNumberOfItems());
            model.addAttribute("totalPages", pageProfile.getBody().getNumberOfPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }
        model.addAttribute("profileCreationRequest", new ProfileCreationRequest());
        model.addAttribute("informationCreationRequest", new InformationCreationRequest());
        return "profile/profile";
    }

    @PostMapping("/create")
    public String createProfile(ProfileCreationRequest profileCreationRequest, RedirectAttributes redirectAttributes) {
        try {
            ResponseEntity<Profile> response = libraryController.createProfile(profileCreationRequest);
            System.out.println("createProfile" + response.getStatusCode());
            redirectAttributes.addFlashAttribute("message", "The Profile has been saved successfully!");
        } catch (Exception e) {
            System.out.println("createProfile ERROR" + e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:";
    }

    @PostMapping("/update")
    public String updateProfile(ProfileCreationRequest profileCreationRequest, RedirectAttributes redirectAttributes) {
        try {
            ResponseEntity<Profile> response = libraryController.updateProfile(profileCreationRequest.getId(), profileCreationRequest);
            logger.info("Profile updated!: ID : " + response.getBody().getId());
            redirectAttributes.addFlashAttribute("message", "The Profile has been updated successfully!");
        } catch (Exception e) {
            logger.info("Profile not updated! ERROR : " + e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:";
    }
    @GetMapping("/fetch/{id}")
    @ResponseBody
    public Optional<ProfileCreationRequest> fetch(@PathVariable("id") Long id) {
        logger.info("Profile has been fetched. Profile id: " + id);
        Profile profile = repositoryService.getProfile(id);
        ProfileCreationRequest request = new ProfileCreationRequest();
        request.setId(profile.getId());
        request.setName(profile.getName());
        request.setDate(profile.getDateAsString());
        return Optional.ofNullable(request);
    }

    @GetMapping("/delete/{id}")
    public String deleteProfile(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            repositoryService.deleteProfile(id);
            redirectAttributes.addFlashAttribute("message", "The Profile with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:../";
    }

}
