package com.example.demo.controller.user;

import com.example.demo.controller.LibraryController;
import com.example.demo.model.InfoType;
import com.example.demo.model.Information;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.draw.Shape;
import com.example.demo.model.request.InformationCreationRequest;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.individual.IndividualPanelsService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/user/canvas")
public class CanvasController {
    private static final Logger logger = LoggerFactory.getLogger(CanvasController.class);
    @Autowired
    private final RepositoryService repositoryService;
    private final List<Shape> shapes = new ArrayList<>();
    @Autowired
    private final IndividualPanelsService individualPanelsService;
    @Autowired
    private final SerialCommunication serialCommunication;

    @Autowired
    private LibraryController libraryController;

    @GetMapping("")
    public String getDraw(Model model) {
        List<Panel> list = repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE);
        model.addAttribute("panels", list);
        return "user/canvas";
    }

    @PostMapping("/drawShape")
    @ResponseBody
    public List<Shape> drawShape(@RequestParam("shapeType") String shapeType, @RequestParam("size") int size, @RequestParam("x") int x, @RequestParam("y") int y) {
        Shape shape = new Shape(size, x, y, shapeType);
        shapes.add(shape);
        return shapes;
    }

    @PostMapping("/sendShape")
    @ResponseBody
    public List<Shape> sendShape(@RequestParam("panelId") int panelId) {
        if (!shapes.isEmpty()) {
            Panel panel = repositoryService.getPanel((long) panelId);
            individualPanelsService.execute(shapes, serialCommunication.getIndexFromDevice(panel.getDevice()));
        }
        return shapes;
    }
    @PostMapping("/sendString")
    @ResponseBody
    public ResponseEntity sendString(@RequestParam("panelId") int panelId, @RequestParam("string") String string) {
        if(string!=null){
            if(string.startsWith("R") //R 10 49 20 69 100 900\n
                    || string.startsWith("C")
                    || string.startsWith("Q\\n")
                    || string.startsWith("E")
                    || string.startsWith("I")
                    || string.startsWith("?")
                    || string.startsWith("H")
                    || string.startsWith("0x89")
                    || string.startsWith("O")
            ) {
                serialCommunication.runSerial(string, panelId);
            } else {
                return ResponseEntity.ok("unreadable input");
            }
        }
        return ResponseEntity.ok("done");
    }

    @GetMapping("/reset")
    public String resetShape() {
        shapes.clear();
        individualPanelsService.clearAllScreens();
        return "redirect:";
    }

    @PostMapping("/saveImage")
    private ResponseEntity saveImage(@RequestParam String file,
                                     @RequestParam String data) {
        String imagePath = String.format("D:\\test/%s", file);
        if(!OSValidator.isWindows()){
            imagePath =  String.format("/home/mte/Application/Canvas/%s", file);;
        }
        if (FileUtils.saveToImageFile(data, imagePath)) {
            try {
                InformationCreationRequest informationCreationRequest = new InformationCreationRequest();
                informationCreationRequest.setName(FileUtils.getInfoNameFromFileName(file));
                informationCreationRequest.setInfoType(InfoType.IMAGE.toString());
                informationCreationRequest.setFileURL(imagePath);
                libraryController.createInformation(informationCreationRequest);
            } catch (Exception e) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image saved but failed to add information : " + e);
            }
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save image");
        }
        return ResponseEntity.ok(String.format("Image saved successfully: %s and added information", file));
    }
}