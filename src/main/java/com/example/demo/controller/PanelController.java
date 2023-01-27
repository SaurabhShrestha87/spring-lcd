package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.model.request.PanelCreationRequest;
import com.example.demo.service.LedService;
import com.example.demo.service.RepositoryService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import lombok.RequiredArgsConstructor;
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
@RequestMapping(value = "/panel")
public class PanelController {
    private final RepositoryService repositoryService;
    LedService ledService = new LedService();
    List<Panel> panelList = new ArrayList<>();
    private LibraryController libraryController;

    @GetMapping("")
    public ModelAndView imageUpload(ModelMap model) {
        libraryController = new LibraryController(repositoryService);
        panelList = FileUtils.getPanelsList();
        System.out.println("Total Panels Found : " + FileUtils.getPanelsList().size());
        model.put("panelList", panelList);
        for (int i = 0; i < panelList.size(); i++) {
            libraryController.updateElseCreatePanel(new PanelCreationRequest(panelList.get(i).getName(), panelList.get(i).getResolution()), panelList.get(i).getId());
        }
        System.out.println(libraryController.getPanels());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("panel/panel");
        return modelAndView;
    }

    @PostMapping("")
    public ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file,
                                           @RequestParam("name") String name,
                                           @RequestParam("type") String type,
                                           @RequestParam("profileId") String profileId) {
        System.out.println("name: " + name);
        System.out.println("name: " + type);
        System.out.println("name: " + file.getOriginalFilename());
        System.out.println("name: " + profileId);
        String fileName = file.getOriginalFilename();
        String filePath = "/home/pi/Application/Uploads/" + fileName;
        try {
            if (OSValidator.isWindows()) {
                file.transferTo(new File("D:\\upload\\" + fileName));
            } else {
                file.transferTo(new File(filePath));
                ledService.setFilePath(filePath);
                ledService.setPanelId("1");
                ledService.run();
            }
        } catch (Exception e) {
            System.out.println("FileUpload Error " + e);
        }
        //TODO This needs to be changed .. microcontroller isnt accepting Byte Array of images.
        //TODO fix this for in app serial communication
        new InformationController(repositoryService).saveInformation(new InformationCreationRequest(name, type, file, filePath, profileId));
        return ResponseEntity.ok(filePath + " File uploaded successfully");
    }
    @GetMapping("/delete/{id}")
    public String deletePanel(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            repositoryService.deletePanel(id);
            redirectAttributes.addFlashAttribute("message", "The Panel with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:information/information";
    }

}
