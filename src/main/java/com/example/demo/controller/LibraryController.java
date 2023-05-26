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
            // If no InfoType is provided, retrieve all information from the repository
            return ResponseEntity.ok(repositoryService.getInformation());
        }
        // If an InfoType is provided, retrieve information based on the specified type from the repository
        return ResponseEntity.ok(repositoryService.getInformation(type));
    }

    @GetMapping("/information/{informationId}")
    public ResponseEntity<Information> getInformation(@PathVariable Long informationId) {
        // Retrieve information by the specified informationId
        return ResponseEntity.ok(repositoryService.getInformation(informationId));
    }

    @GetMapping("/information/base-search")
    public ResponseEntity getBaseInformation(Pageable pageable) {
        // Retrieve base information with sorting based on the provided pageable object
        return ResponseEntity.ok(repositoryService.getBaseInformationWithSorting(pageable));
    }

    @GetMapping("/information/search")
    public ResponseEntity getInformation(Pageable pageable) {
        // Retrieve information with sorting based on the provided pageable object
        return ResponseEntity.ok(repositoryService.getInformationWithSorting(pageable));
    }

    @GetMapping("/information/base-search/filter")
    public ResponseEntity getBaseInformationWithFilter(@RequestParam("query") String query, Pageable pageable) {
        // Retrieve base information with filtering based on the provided query string and sorting based on the pageable object
        return ResponseEntity.ok(repositoryService.filterBaseInformation(query, pageable));
    }
    @GetMapping("/information/search/filter")
    public ResponseEntity getInformationWithFilter(@RequestParam("query") String query, Pageable pageable) {
        // Retrieve information with filtering based on the provided query string and sorting based on the pageable object
        return ResponseEntity.ok(repositoryService.filterInformation(query, pageable));
    }

    @PostMapping("/information")
    public ResponseEntity<Information> createInformation(@RequestBody InformationCreationRequest request) {
        // Create new information based on the provided request object and return it in the response body
        return ResponseEntity.ok(repositoryService.createInformation(request));
    }

    @PatchMapping("/information/{informationId}")
    public ResponseEntity<Information> updateInformation(@PathVariable("informationId") Long informationId, @RequestBody InformationCreationRequest request) {
        // Update the information with the specified informationId using the provided request object
        // Return the updated information in the response body
        return ResponseEntity.ok(repositoryService.updateInformation(informationId, request));
    }

    @DeleteMapping("/information/{id}")
    public ResponseEntity<Void> deleteInformation(@PathVariable("id") Long id) {
        // Delete the information with the specified id
        repositoryService.deleteInformation(id);
        // Return a successful response with no body
        return ResponseEntity.ok().build();
    }
    ////////////Panel//////////////    ////////////Panel//////////////    ////////////Panel//////////////    ////////////Panel//////////////    ////////////Panel//////////////
    @PostMapping("/panel")
    public ResponseEntity<Panel> createPanel(@RequestBody PanelCreationRequest request) {
        // Create a new panel using the provided request object
        // Return the created panel in the response body
        return ResponseEntity.ok(repositoryService.createPanel(request));
    }

    @GetMapping("/panel/search")
    public ResponseEntity<PaginatedPanelResponse> getPanel(Pageable pageable) {
        // Retrieve panels with sorting and pagination based on the provided Pageable object
        // Return the paginated panel response in the response body
        return ResponseEntity.ok(repositoryService.getPanelWithSorting(pageable));
    }

    @GetMapping("/panel/search/filter")
    public ResponseEntity<PaginatedPanelResponse> getPanelWithFilter(@RequestParam("query") String query, Pageable pageable) {
        // Retrieve panels with filtering, sorting, and pagination based on the provided query and Pageable object
        // Return the filtered, sorted, and paginated panel response in the response body
        return ResponseEntity.ok(repositoryService.filterPanel(query, pageable));
    }


    @GetMapping("/panel")
    public ResponseEntity<List<Panel>> getPanels() {
        // Retrieve all panels from the repository service
        // Return the list of panels in the response body
        return ResponseEntity.ok(repositoryService.getPanels());
    }

    @PatchMapping("/panel/{panelId}")
    public ResponseEntity<Panel> updatePanel(@PathVariable Long panelId, @RequestBody PanelCreationRequest request) {
        // Update the panel with the given panelId using the provided request object
        // Return the updated panel in the response body
        return ResponseEntity.ok(repositoryService.updatePanel(panelId, request));
    }

    public ResponseEntity<Panel> updateElseCreatePanel(@RequestBody PanelCreationRequest request, @PathVariable Long panelId) {
        // Update the panel with the given panelId if it exists, otherwise create a new panel using the provided request object
        // Return the updated or created panel in the response body
        return ResponseEntity.ok(repositoryService.updateElseCreatePanel(panelId, request));
    }


    ////////////Lend//////////////   ////////////Lend//////////////   ////////////Lend//////////////   ////////////Lend//////////////   ////////////Lend//////////////
    @GetMapping("lend")
    public ResponseEntity<List<Lend>> getLends() {
        // Retrieve all lends from the repository service
        // Return the list of lends in the response body
        return ResponseEntity.ok(repositoryService.getLend());
    }

    @GetMapping("lend/{lendId}")
    public ResponseEntity<Lend> getLend(@PathVariable Long lendId) {
        // Retrieve the lend with the given lendId from the repository service
        // Return the lend in the response body
        return ResponseEntity.ok(repositoryService.getLend(lendId));
    }

    @GetMapping("lend/search")
    public ResponseEntity getLend(Pageable pageable) {
        // Retrieve lends with sorting and pagination based on the provided Pageable object
        // Return the paginated lends in the response body
        return ResponseEntity.ok(repositoryService.getLendWithSorting(pageable));
    }

    @GetMapping("lend/search/filter")
    public ResponseEntity filterLendWithPanelId(@RequestParam("query") Long panelId, Pageable pageable) {
        // Filter lends based on the provided panelId and pagination parameters
        // Return the filtered and paginated lends in the response body
        return ResponseEntity.ok(repositoryService.filterLendWithPanelId(panelId, pageable));
    }

    @PostMapping("lend")
    public ResponseEntity<List<String>> lendAProfile(@RequestBody ProfileLendRequest profileLendRequests) {
        // This method is used to lend a profile to a user based on the given request.
        return ResponseEntity.ok(repositoryService.lendAProfile(profileLendRequests));
    }

    @GetMapping("lendAll")
    public ResponseEntity<List<List<Lend>>> lendAll() {
        List<List<Lend>> AllLendList = new ArrayList<>();
        // Retrieve all active panels from the panel repository
        for (Panel allByStatus : panelRepository.findAllByStatus(PanelStatus.ACTIVE)) {
            // Find all lends associated with the current panel that have a status of "RUNNING"
            List<Lend> lendList = lendRepository.findAllByPanelIdAndStatus(allByStatus.getId(), LendStatus.RUNNING);
            // Add the lend list to the overall lend list
            AllLendList.add(lendList);
        }
        // Return the overall lend list as a response
        return ResponseEntity.ok(AllLendList);
    }


    ///////PROFILE//////////////PROFILE///////////////////PROFILE////////////////PROFILE////////////////PROFILE///////////
    @PostMapping("/profile")
    public ResponseEntity<Profile> createProfile(@RequestBody ProfileCreationRequest request) {
        try {
            // This method is used to create a new profile based on the given request.
            return ResponseEntity.ok(repositoryService.createProfile(request));
        } catch (ParseException e) {
            // If there's an error parsing the request, a RuntimeException is thrown.
            // This handles the exception and rethrows it as a RuntimeException.
            throw new RuntimeException(e);
        }
    }

    @PatchMapping("/profile/{profileId}")
    public ResponseEntity<Profile> updateProfile(@PathVariable("profileId") Long profileId, @RequestBody ProfileCreationRequest request) {
        try {
            // This method is used to update an existing profile with the given profileId and request.
            return ResponseEntity.ok(repositoryService.updateProfile(profileId, request));
        } catch (ParseException e) {
            // If there's an error parsing the request, a RuntimeException is thrown.
            // This handles the exception and rethrows it as a RuntimeException.
            throw new RuntimeException(e);
        }
    }


    @GetMapping("profile")
    public ResponseEntity<List<Profile>> getProfile() {
        // This method is used to retrieve all profiles.
        return ResponseEntity.ok(repositoryService.getProfile());
    }

    @GetMapping("profile/{profileId}")
    public ResponseEntity<Profile> getProfile(@PathVariable Long profileId) {
        // This method is used to retrieve a specific profile based on the given profileId.
        return ResponseEntity.ok(repositoryService.getProfile(profileId));
    }

    @GetMapping("profile/search")
    public ResponseEntity<PaginatedProfileResponse> getProfile(Pageable pageable) {
        // This method is used to search for profiles based on the provided pageable information.
        // It returns a paginated response containing the requested profiles.
        return ResponseEntity.ok(repositoryService.getProfile(pageable));
    }


    @GetMapping("profile/search/filter")
    public ResponseEntity<PaginatedProfileResponse> getProfileWithFilter(@RequestParam("query") String query, Pageable pageable) {
        // This method is used to search for profiles based on the provided query and pageable information.
        // It applies the filter query to retrieve relevant profiles and returns a paginated response.
        return ResponseEntity.ok(repositoryService.filterProfile(query, pageable));
    }

    public ResponseEntity<Information> createInformationForProfile(ProfileAddInformationRequest profileCreationRequest) {
        // This method is used to create an information entry for a profile.
        // It retrieves the current information based on the provided informationId,
        // creates a new information object, copies properties from the current information,
        // sets the count, duration, and profile based on the given request,
        // and returns the created information.
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
        // This method is used to create information.
        // It invokes the repository service to create the information entry.
        // It returns a response entity with no body.
        repositoryService.createInformation();
        return ResponseEntity.ok().build();
    }

    ///////SETTING//////////////SETTING///////////////////SETTING////////////////SETTING////////////////SETTING///////////

    @GetMapping("/settings")
    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }
}
