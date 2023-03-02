package com.example.demo.controller;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.draw.Shape;
import com.example.demo.service.individual.IndividualLedService;
import com.example.demo.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final IndividualLedService individualLedService;
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
            individualLedService.execute(shapes, repositoryService.getPanel((long) panelId));
        }
        return shapes;
    }

    @GetMapping("/reset")
    public String resetShape() {
        shapes.clear();
        individualLedService.clearAllScreens();
        return "redirect:";
    }
}