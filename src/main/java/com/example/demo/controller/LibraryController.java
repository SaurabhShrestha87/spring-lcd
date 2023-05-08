package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.model.request.*;
import com.example.demo.model.response.PaginatedPanelResponse;
import com.example.demo.model.response.PaginatedProfileResponse;
import com.example.demo.model.setting.Setting;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
import com.example.demo.repository.SettingRepository;
import com.example.demo.service.RepositoryService;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@NoArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/api")
public class LibraryController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private LendRepository lendRepository;
    @Autowired
    private PanelRepository panelRepository;

    @Autowired
    private SettingRepository settingRepository;

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

    @GetMapping("/information/base-search")
    public ResponseEntity getBaseInformation(Pageable pageable) {
        return ResponseEntity.ok(repositoryService.getBaseInformationWithSorting(pageable));
    }

    @GetMapping("/information/search")
    public ResponseEntity getInformation(Pageable pageable) {
        return ResponseEntity.ok(repositoryService.getInformationWithSorting(pageable));
    }

    @GetMapping("/information/base-search/filter")
    public ResponseEntity getBaseInformationWithFilter(@RequestParam("query") String query, Pageable pageable) {
        return ResponseEntity.ok(repositoryService.filterBaseInformation(query, pageable));
    }

    @GetMapping("/information/search/filter")
    public ResponseEntity getInformationWithFilter(@RequestParam("query") String query, Pageable pageable) {
        return ResponseEntity.ok(repositoryService.filterInformation(query, pageable));
    }

    @PostMapping("/information")
    public ResponseEntity<Information> createInformation(@RequestBody InformationCreationRequest request) {
        return ResponseEntity.ok(repositoryService.createInformation(request));
    }

    @PatchMapping("/information/{informationId}")
    public ResponseEntity<Information> updateInformation(@PathVariable("informationId") Long informationId, @RequestBody InformationCreationRequest request) {
        return ResponseEntity.ok(repositoryService.updateInformation(informationId, request));
    }

    @DeleteMapping("/information/{id}")
    public ResponseEntity<Void> deleteInformation(@PathVariable("id") Long id) {
        repositoryService.deleteInformation(id);
        return ResponseEntity.ok().build();
    }

    ////////////Panel//////////////    ////////////Panel//////////////    ////////////Panel//////////////    ////////////Panel//////////////    ////////////Panel//////////////
    @PostMapping("/panel")
    public ResponseEntity<Panel> createPanel(@RequestBody PanelCreationRequest request) {
        return ResponseEntity.ok(repositoryService.createPanel(request));
    }


    @GetMapping("/panel/search")
    public ResponseEntity<PaginatedPanelResponse> getPanel(Pageable pageable) {
        return ResponseEntity.ok(repositoryService.getPanelWithSorting(pageable));
    }

    @GetMapping("/panel/search/filter")
    public ResponseEntity<PaginatedPanelResponse> getPanelWithFilter(@RequestParam("query") String query, Pageable pageable) {
        return ResponseEntity.ok(repositoryService.filterPanel(query, pageable));
    }


    @GetMapping("/panel")
    public ResponseEntity<List<Panel>> getPanels() {
        return ResponseEntity.ok(repositoryService.getPanels());
    }

    @PatchMapping("/panel/{panelId}")
    public ResponseEntity<Panel> updatePanel(@PathVariable Long panelId, @RequestBody PanelCreationRequest request) {
        return ResponseEntity.ok(repositoryService.updatePanel(panelId, request));
    }

    public ResponseEntity<Panel> updateElseCreatePanel(@RequestBody PanelCreationRequest request, @PathVariable Long panelId) {
        return ResponseEntity.ok(repositoryService.updateElseCreatePanel(panelId, request));
    }


    ////////////Lend//////////////   ////////////Lend//////////////   ////////////Lend//////////////   ////////////Lend//////////////   ////////////Lend//////////////
    @GetMapping("lend")
    public ResponseEntity<List<Lend>> getLends() {
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
    public ResponseEntity filterLendWithPanelId(@RequestParam("query") Long panelId, Pageable pageable) {
        return ResponseEntity.ok(repositoryService.filterLendWithPanelId(panelId, pageable));
    }

    @PostMapping("lend")
    public ResponseEntity<List<String>> lendAProfile(@RequestBody ProfileLendRequest profileLendRequests) {
        return ResponseEntity.ok(repositoryService.lendAProfile(profileLendRequests));
    }

    @GetMapping("lendAll")
    public ResponseEntity<List<List<Lend>>> lendAll() {
        List<List<Lend>> AllLendList = new ArrayList<>();
        for (Panel allByStatus : panelRepository.findAllByStatus(PanelStatus.ACTIVE)) {
            List<Lend> lendList = lendRepository.findAllByPanelIdAndStatus(allByStatus.getId(), LendStatus.RUNNING);
            AllLendList.add(lendList);
        }
        return ResponseEntity.ok(AllLendList);
    }

    ///////PROFILE//////////////PROFILE///////////////////PROFILE////////////////PROFILE////////////////PROFILE///////////
    @PostMapping("/profile")
    public ResponseEntity<Profile> createProfile(@RequestBody ProfileCreationRequest request) {
        try {
            return ResponseEntity.ok(repositoryService.createProfile(request));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @PatchMapping("/profile/{profileId}")
    public ResponseEntity<Profile> updateProfile(@PathVariable("informationId") Long profileId, @RequestBody ProfileCreationRequest request) {
        try {
            return ResponseEntity.ok(repositoryService.updateProfile(profileId, request));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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
    public ResponseEntity<PaginatedProfileResponse> getProfile(Pageable pageable) {
        return ResponseEntity.ok(repositoryService.getProfile(pageable));
    }

    @GetMapping("profile/search/filter")
    public ResponseEntity<PaginatedProfileResponse> getProfileWithFilter(@RequestParam("query") String query, Pageable pageable) {
        return ResponseEntity.ok(repositoryService.filterProfile(query, pageable));
    }

    public ResponseEntity<Information> createInformationForProfile(ProfileAddInformationRequest profileCreationRequest) {
        Information currentInformation = repositoryService.getInformation(profileCreationRequest.getInformationId());
        Information informationToCreate = new Information();
        BeanUtils.copyProperties(currentInformation, informationToCreate);
        informationToCreate.setId(0L);
        informationToCreate.setCount(profileCreationRequest.getCount());
        informationToCreate.setDuration(profileCreationRequest.getDuration());
        informationToCreate.setProfile(repositoryService.getProfile(profileCreationRequest.getProfileId()));
        return ResponseEntity.ok(repositoryService.createInformation(informationToCreate));
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createInformation() {
        repositoryService.createInformation();
        return ResponseEntity.ok().build();
    }


    ///////SETTING//////////////SETTING///////////////////SETTING////////////////SETTING////////////////SETTING///////////

    @GetMapping("/settings")
    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }
}
