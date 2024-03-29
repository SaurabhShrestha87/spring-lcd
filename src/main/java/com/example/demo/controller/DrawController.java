package com.example.demo.controller;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.draw.Shape;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.individual.IndividualPanelsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/draw")
public class DrawController {
    private static final Logger logger = LoggerFactory.getLogger(DrawController.class);
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
        return "draw/drawshapes";
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
}