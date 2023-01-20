package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.request.InformationLendRequest;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.model.request.ProfileCreationRequest;
import com.example.demo.service.repositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/api")
public class LibraryController {

    private final repositoryService repositoryService;

    @GetMapping("/information")
    public ResponseEntity getInformation(@RequestParam(required = false) InfoType type) {
        if (type == null) {
            return ResponseEntity.ok(repositoryService.getInformation());
        }
        return ResponseEntity.ok(repositoryService.getInformation(type));
    }

    @GetMapping("/information/{informationId}")
    public ResponseEntity<Information> getInformation(@PathVariable Long informationId) {
        return ResponseEntity.ok(repositoryService.getInformation(informationId));
    }

    @GetMapping("/information/search")
    public ResponseEntity getInformation(Pageable pageable) {
        return ResponseEntity.ok(repositoryService.getInformationWithSorting(pageable));
    }

    @GetMapping("/information/search/filter")
    public ResponseEntity getInformationWithFilter(@RequestParam("query") String query, Pageable pageable) {
        return ResponseEntity.ok(repositoryService.filterInformation(query, pageable));
    }

    @PostMapping("/information")
    public ResponseEntity<Information> createBook(@RequestBody InformationCreationRequest request) {
        return ResponseEntity.ok(repositoryService.createInformation(request));
    }


    @PatchMapping("/information/{informationId}")
    public ResponseEntity<Information> updateBook(@PathVariable("informationId") Long informationId, @RequestBody InformationCreationRequest request) {
        return ResponseEntity.ok(repositoryService.updateInformation(informationId, request));
    }

    @DeleteMapping("/information/{informationId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long informationId) {
        repositoryService.deleteInformation(informationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/panel")
    public ResponseEntity<Panel> createPanel(@RequestBody PanelCreationRequest request) {
        return ResponseEntity.ok(repositoryService.createPanel(request));
    }

    @GetMapping("/panel")
    public ResponseEntity<List<Panel>> getPanels() {
        return ResponseEntity.ok(repositoryService.readPanels());
    }

    @PatchMapping("/panel/{panelId}")
    public ResponseEntity<Panel> updatePanel(@RequestBody PanelCreationRequest request, @PathVariable Long panelId) {
        return ResponseEntity.ok(repositoryService.updatePanel(panelId, request));
    }

    @PostMapping("lend")
    public ResponseEntity<List<String>> lendAInformation(@RequestBody InformationLendRequest informationLendRequests) {
        return ResponseEntity.ok(repositoryService.lendAInformation(informationLendRequests));
    }

    @GetMapping("lend")
    public ResponseEntity getLends() {
        return ResponseEntity.ok(repositoryService.getLend());
    }

    @GetMapping("lend/{lendId}")
    public ResponseEntity<Lend> getLend(@PathVariable Long lendId) {
        return ResponseEntity.ok(repositoryService.getLend(lendId));
    }

    @GetMapping("lend/search")
    public ResponseEntity getLend(Pageable pageable) {
        return ResponseEntity.ok(repositoryService.getLendWithSorting(pageable));
    }

    @GetMapping("lend/search/filter")
    public ResponseEntity getLend(@RequestParam("query") Long query, Pageable pageable) {
        return ResponseEntity.ok(repositoryService.filterLendWithPanelId(query, pageable));
    }

    ///////PROFILE//////////////PROFILE///////////////////PROFILE////////////////PROFILE////////////////PROFILE///////////
    @PostMapping("/profile")
    public ResponseEntity<Profile> createProfile(@RequestBody ProfileCreationRequest request) {
        return ResponseEntity.ok(repositoryService.createProfile(request));
    }

    @GetMapping("profile")
    public ResponseEntity<List<Profile>> getProfile() {
        return ResponseEntity.ok(repositoryService.getProfile());
    }

    @GetMapping("profile/{profileId}")
    public ResponseEntity<Profile> getProfile(@PathVariable Long profileId) {
        return ResponseEntity.ok(repositoryService.getProfile(profileId));
    }

    @GetMapping("profile/search")
    public ResponseEntity getProfile(Pageable pageable) {
        return ResponseEntity.ok(repositoryService.getProfile(pageable));
    }

    @GetMapping("profile/search/filter")
    public ResponseEntity getProfileWithFilter(@RequestParam("query") String query, Pageable pageable) {
        return ResponseEntity.ok(repositoryService.filterProfile(query, pageable));
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createInformation() {
        repositoryService.createInformation();
        return ResponseEntity.ok().build();
    }

}
