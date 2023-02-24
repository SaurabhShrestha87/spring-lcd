package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.model.request.*;
import com.example.demo.model.response.*;
import com.example.demo.repository.InformationRepository;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
import com.example.demo.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class RepositoryService {
    @Autowired
    private final ProfileRepository profileRepository;
    @Autowired
    private final PanelRepository panelRepository;
    @Autowired
    private final LendRepository lendRepository;
    @Autowired
    private final InformationRepository informationRepository;

//////////Information/////////////    //////////Information/////////////    //////////Information/////////////    //////////Information/////////////
    public Information getInformation(Long id) {
        Optional<Information> book = informationRepository.findById(id);
        if (book.isPresent()) {
            return book.get();
        }
        throw new EntityNotFoundException("Cant find any book under given ID");
    }
    public List<Information> getInformation() {
        return informationRepository.findAll();
    }

    public List<Information>  getBaseInformation() {
        Optional<List<Information>> information = Optional.ofNullable(informationRepository.findAllByProfileIsNull());
        if (information.isPresent()) {
            return information.get();
        }
        throw new EntityNotFoundException("Cant find any BaseInformations");
    }

    public Information getInformation(InfoType type) {
        Optional<Information> book = informationRepository.findByType(type);
        if (book.isPresent()) {
            return book.get();
        }
        throw new EntityNotFoundException("Cant find any book under given ISBN");
    }
    public Information createInformation(InformationCreationRequest informationCreationRequest) {
        Information informationToCreate = new Information();
        informationToCreate.setName(informationCreationRequest.getName());
        informationToCreate.setType(InfoType.valueOf(informationCreationRequest.getInfoType()));
        informationToCreate.setUrl(informationCreationRequest.getFileURLFromMultipart());
        return informationRepository.save(informationToCreate);
    }
    public Information createInformation(Information informationToCreate) {
        return informationRepository.save(informationToCreate);
    }
    public void deleteInformation(Long id) {
        informationRepository.deleteById(id);
    }
    public Information updateInformation(Long bookId, InformationCreationRequest request) {
        Optional<Information> optionalInformation = informationRepository.findById(bookId);
        if (optionalInformation.isEmpty()) {
            throw new EntityNotFoundException("Information Not Found");
        }
        Information information = optionalInformation.get();
        information.setType(InfoType.valueOf(request.getInfoType()));
        information.setName(request.getName());
        information.setUrl(request.getFileURLFromMultipart());
        information.setDuration(String.valueOf(request.getDuration()));
        information.setCount(String.valueOf(request.getCount()));
        return informationRepository.save(information);
    }
    public PaginatedInformationResponse getBaseInformationWithSorting(Pageable pageable) {
        Page<Information> baseInformation = informationRepository.findAllByProfileIsNull(pageable);
        return PaginatedInformationResponse.builder()
                .numberOfItems(baseInformation.getTotalElements()).numberOfPages(baseInformation.getTotalPages())
                .informationList(baseInformation.getContent())
                .build();
    }
    public PaginatedInformationResponse getInformationWithSorting(Pageable pageable) {
        Page<Information> information = informationRepository.findAll(pageable);
        return PaginatedInformationResponse.builder()
                .numberOfItems(information.getTotalElements()).numberOfPages(information.getTotalPages())
                .informationList(information.getContent())
                .build();
    }
    public PaginatedInformationResponse filterBaseInformation(String name, Pageable pageable) {
        Page<Information> baseInformation = informationRepository.findAllByNameContainsAndProfileIsNull(name, pageable);
        return PaginatedInformationResponse.builder()
                .numberOfItems(baseInformation.getTotalElements()).numberOfPages(baseInformation.getTotalPages())
                .informationList(baseInformation.getContent())
                .build();
    }
    public PaginatedInformationResponse filterInformation(String name, Pageable pageable) {
        Page<Information> information = informationRepository.findAllByNameContains(name, pageable);
        return PaginatedInformationResponse.builder()
                .numberOfItems(information.getTotalElements()).numberOfPages(information.getTotalPages())
                .informationList(information.getContent())
                .build();
    }

//////////Profile/////////////    //////////Profile/////////////    //////////Profile/////////////    //////////Profile/////////////    //////////Profile/////////////
    @SneakyThrows
    public Profile createProfile(ProfileCreationRequest request) {
        Profile profile = new Profile();
        profile.setId(request.getId());
        profile.setName(request.getName());
        profile.setDate(request.getInstantDate());
        return profileRepository.save(profile);
    }
    @SneakyThrows
    public Profile updateProfile(Long id, ProfileCreationRequest request) {
        Optional<Profile> optionalProfile = profileRepository.findById(id);
        if (optionalProfile.isEmpty()) {
            throw new EntityNotFoundException("Profile not present in the database");
        }
        Profile profile = optionalProfile.get();
        profile.setName(request.getName());
        profile.setDate(request.getInstantDate());
        return profileRepository.save(profile);
    }
    public List<Profile> getProfile() {
        return profileRepository.findAll();
    }
    public Profile getProfile(Long id) {
        Optional<Profile> profile = profileRepository.findById(id);
        if (profile.isPresent()) {
            return profile.get();
        }
        throw new EntityNotFoundException("Cant find any book under given ID");
    }
    public PaginatedProfileResponse filterProfile(String name, Pageable pageable) {
        Page<Profile> profile = profileRepository.findAllByName(name, pageable);
        return PaginatedProfileResponse.builder()
                .numberOfItems(profile.getTotalElements()).numberOfPages(profile.getTotalPages())
                .profileList(profile.getContent())
                .build();
    }
    public PaginatedProfileResponse getProfile(Pageable pageable) {
        Page<Profile> profile = profileRepository.findAll(pageable);
        return PaginatedProfileResponse.builder()
                .numberOfItems(profile.getTotalElements()).numberOfPages(profile.getTotalPages())
                .profileList(profile.getContent())
                .build();
    }
    public void deleteProfile(Long id) {
        profileRepository.deleteById(id);
    }

//////////LEND/////////////  //////////LEND/////////////  //////////LEND/////////////  //////////LEND/////////////  //////////LEND/////////////
    public List<Lend> getLend() {
        return lendRepository.findAll();
    }
    public Lend getLend(Long id) {
        Optional<Lend> lend = lendRepository.findById(id);
        if (lend.isPresent()) {
            return lend.get();
        }
        throw new EntityNotFoundException("Cant find any lend under given id");
    }
    public PaginatedLendResponse getLendWithSorting(Pageable pageable) {
        Page<Lend> lend = lendRepository.findAll(pageable);
        return PaginatedLendResponse.builder()
                .numberOfItems(lend.getTotalElements()).numberOfPages(lend.getTotalPages())
                .lendList(lend.getContent())
                .build();
    }

    public PaginatedLendResponse filterLendWithPanelId(Long panelId, Pageable pageable) {
        Optional<Panel> panel = panelRepository.findById(panelId);
        if (panel.isPresent()) {
            Page<Lend> lend = lendRepository.findAllByPanelContains(panel.get(), pageable);
            return PaginatedLendResponse.builder()
                    .numberOfItems(lend.getTotalElements()).numberOfPages(lend.getTotalPages())
                    .lendList(lend.getContent())
                    .build();
        }
        throw new EntityNotFoundException("Cant find any lend under given id");
    }
    public List<String> lendAProfile(ProfileLendRequest request) {

        Optional<Panel> memberForId = panelRepository.findById(request.getPanelId());
        if (!memberForId.isPresent()) {
            System.out.println("Panel not present in the database");
            throw new EntityNotFoundException("Panel not present in the database");
        }
        Panel panel = memberForId.get();
        if (panel.getStatus() != PanelStatus.ACTIVE) {
            System.out.println("Panel is not active to proceed a lending.");
            throw new RuntimeException("Panel is not active to proceed a lending.");
        }
        List<String> profileApprovedToLend = new ArrayList<>();
        request.getProfileIds().forEach(profileId -> {
            Optional<Profile> profileForId = profileRepository.findById(profileId);
            if (!profileForId.isPresent()) {
                System.out.println("Cant find any profile under given ID");
                throw new EntityNotFoundException("Cant find any profile under given ID");
            }
            profileApprovedToLend.add(profileForId.get().getName());
            Lend lend = new Lend();
            lend.setPanel(memberForId.get());
            lend.setProfile(profileForId.get());
            lend.setStatus(LendStatus.AVAILABLE); // TODO be able to turn lend RUNNING...
            lend.setStartOn(Instant.now());
            lend.setDueOn(Instant.now().plus(10, ChronoUnit.SECONDS));
            System.out.println(lendRepository.save(lend));
        });
        return profileApprovedToLend;
    }
    public void deleteLend(Long id) {
        lendRepository.deleteById(id);
    }

    public List<Panel> getPanelsWithStatus(PanelStatus status) {
        List<Panel> optionalPanel = panelRepository.findAllByStatus(status);
        if (optionalPanel != null) {
            return optionalPanel;
        }
        throw new EntityNotFoundException("Cant find any Active Panels");
    }


//////////Panel/////////////    //////////Panel/////////////    //////////Panel/////////////    //////////Panel/////////////    //////////Panel/////////////
    public Panel createPanel(PanelCreationRequest request) {
        Panel panel = new Panel();
        BeanUtils.copyProperties(request, panel);
        panel.setStatus(PanelStatus.ACTIVE);
        return panelRepository.save(panel);
    }
    public Panel getPanel(Long id) {
        Optional<Panel> panelOptional = panelRepository.findById(id);
        if (panelOptional.isPresent()) {
            return panelOptional.get();
        }
        throw new EntityNotFoundException("Cant find any Panel under given ID");
    }
    public PaginatedPanelResponse getPanelWithSorting(Pageable pageable) {
        Page<Panel> panelPage= panelRepository.findAll(pageable);
        return PaginatedPanelResponse.builder()
                .numberOfItems(panelPage.getTotalElements())
                .numberOfPages(panelPage.getTotalPages())
                .panelList(panelPage.getContent())
                .build();
    }
    public PaginatedPanelResponse filterPanel(String name, Pageable pageable) {
        Page<Panel> panelP = panelRepository.findAllByNameContains(name, pageable);
        return PaginatedPanelResponse.builder()
                .numberOfItems(panelP.getTotalElements())
                .numberOfPages(panelP.getTotalPages())
                .panelList(panelP.getContent())
                .build();
    }
    public Panel updatePanel(Long id, PanelCreationRequest request) {
        Optional<Panel> optionalMember = panelRepository.findById(id);
        if (!optionalMember.isPresent()) {
            throw new EntityNotFoundException("Member not present in the database");
        }
        Panel panel = optionalMember.get();
        panel.setName(request.getName());
        panel.setResolution(request.getResolution());
        return panelRepository.save(panel);
    }
    public Panel updateElseCreatePanel(Long id, PanelCreationRequest request) {
        Optional<Panel> optionalMember = panelRepository.findById(id);
        if (!optionalMember.isPresent()) {
            return createPanel(request);
        }
        Panel panel = optionalMember.get();
        panel.setName(request.getName());
        panel.setResolution(request.getResolution());
        return panelRepository.save(panel);
    }
    public List<Panel> readPanels() {
        return panelRepository.findAll();
    }
    public void deletePanel(Long id) {
        lendRepository.deleteById(id);
    }
//////BULK INFO/////////    //////BULK INFO/////////    //////BULK INFO/////////    //////BULK INFO/////////    //////BULK INFO/////////    //////BULK INFO/////////
    public void createInformation() {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);
        ResponseEntity<List<ProfileResponseFromAPI>> bookResponse =
                restTemplate.exchange("https://raw.githubusercontent.com/bvaughn/infinite-list-reflow-examples/master/books.json",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<ProfileResponseFromAPI>>() {
                        });

        List<ProfileResponseFromAPI> booksFromAPI = bookResponse.getBody();
        booksFromAPI.forEach(book -> {
            Profile profile = profileRepository.findAll().get(0);
            Information information1 = new Information();
            information1.setUrl(book.getFileUrl());
            information1.setProfile(profile);
            information1.setName(book.getTitle());
            information1.setType(book.getType());
            informationRepository.save(information1);
        });
    }
}
