package com.example.demo.controller;


import com.example.demo.model.Information;
import com.example.demo.model.Lend;
import com.example.demo.model.Panel;
import com.example.demo.model.Profile;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.model.response.PaginatedInformationResponse;
import com.example.demo.model.response.PaginatedLendResponse;
import com.example.demo.model.response.PaginatedProfileResponse;
import com.example.demo.service.LedService;
import com.example.demo.service.repositoryService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import com.pi4j.io.serial.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.DemoApplication.SERIAL_DEVICE;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
public class UiController {
    private final repositoryService repositoryService;
    LedService ledService = new LedService();
    List<Panel> panelList = new ArrayList<>();
    private LibraryController libraryController;
    private Serial serial;

    @GetMapping("/information")
    public String getAll(Model model,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "3") int size) {
        try {
            libraryController = new LibraryController(repositoryService);
            List<Information> information;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedInformationResponse> pageInformation;

            if (keyword == null) {
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

        return "info";
    }

    @GetMapping("/lend")
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

    @GetMapping("/profile")
    public String getProfile(Model model,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "3") int size) {
        try {
            libraryController = new LibraryController(repositoryService);
            List<Profile> profileList;
            Pageable paging = PageRequest.of(page - 1, size);
            ResponseEntity<PaginatedProfileResponse> pageProfile;

            if (keyword == null) {
                pageProfile = libraryController.getProfile(paging);
            } else {
                pageProfile = libraryController.getProfileWithFilter(keyword, paging);
                model.addAttribute("keyword", keyword);
            }
            profileList = pageProfile.getBody().getProfileList();
            for (Profile profile : profileList) {
                System.out.println(profile.toString());
            }
            model.addAttribute("profiles", profileList);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalItems", pageProfile.getBody().getNumberOfItems());
            model.addAttribute("totalPages", pageProfile.getBody().getNumberOfPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }

        return "profile";
    }

    @GetMapping("/upload")
    public ModelAndView imageUpload(ModelMap model) {
        libraryController = new LibraryController(repositoryService);
        panelList = FileUtils.getPanelsList();
        System.out.println("Total Panels Found : " + FileUtils.getPanelsList().size());
        model.put("panelList", panelList);
        for (int i = 0; i < panelList.size(); i++) {
            libraryController.updatePanel(new PanelCreationRequest(panelList.get(i).getName(), panelList.get(i).getResolution()), panelList.get(i).getId());
        }
        System.out.println(libraryController.getPanels());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("upload");
        return modelAndView;
    }

    @PostMapping("/upload")
    public ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file,
                                           @RequestParam("panel") String panelId) {
        System.out.println("upload at panelId : " + panelId);
        String fileName = file.getOriginalFilename();
        String filePath = "/home/pi/Application/Uploads/" + fileName;
        try {
            if (OSValidator.isWindows()) {
                file.transferTo(new File("D:\\upload\\" + fileName));
            } else {
                file.transferTo(new File(filePath));
                ledService.setFilePath(filePath);
                ledService.setPanelId(panelId);
                ledService.run();
            }
        } catch (Exception e) {
            System.out.println("FileUpload Error " + e);
        }
        try {
            //TODO This needs to be changed .. microcontroller isnt accepting Byte Array of images.
            //TODO fix this for in app serial communication
//			LedInit();
//			Thread t = new Thread(new SerialSender(serial, imageByteArray));
//			t.start();
        } catch (Exception e) {
            System.out.println("LedInit Error : " + e);
        }
        return ResponseEntity.ok(filePath + " File uploaded successfully at " + panelId);
    }

    private void LedInit() {
        // Create an instance of the serial communications class
        serial = SerialFactory.createInstance();
        // Create and register the serial data listener
        try {
            // Create serial config object
            SerialConfig config = new SerialConfig();
            config.device(SERIAL_DEVICE)
                    .baud(Baud._38400)
                    .dataBits(DataBits._8)
                    .parity(Parity.NONE)
                    .stopBits(StopBits._1)
                    .flowControl(FlowControl.NONE);

            // Display connection details
            System.out.println("Connection: " + config);
            // Open the serial port with the configuration
            serial.open(config);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

}
