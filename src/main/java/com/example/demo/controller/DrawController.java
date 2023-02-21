package com.example.demo.controller;

import com.example.demo.model.PanelStatus;
import com.example.demo.model.draw.Shape;
import com.example.demo.service.DrawService;
import com.example.demo.service.LedService;
import com.example.demo.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/draw")
public class DrawController {
    private static final Logger logger = LoggerFactory.getLogger(InformationController.class);
    private final RepositoryService repositoryService;
    private final List<Shape> shapes = new ArrayList<>();
    @Autowired
    private final LedService ledService;
    private LibraryController libraryController;

    @GetMapping("")
    public String getDraw() {
        return "draw/drawshapes";
    }

    @PostMapping("/drawShape")
    @ResponseBody
    public List<Shape> drawShape(@RequestParam("size") int size, @RequestParam("x") int x, @RequestParam("y") int y) {
        logger.info("This RAN!");
        Shape shape = new Shape(size, x, y, "square");
        shapes.add(shape);
        logger.info("RETURNING SHAPE");
        return shapes;
    }

    @PostMapping("/sendShape")
    @ResponseBody
    public List<Shape> sendShape(@RequestParam("size") int size, @RequestParam("x") int x, @RequestParam("y") int y) {
        logger.info("printing SHAPE");
        ledService.execute(shapes, repositoryService.getPanelsWithStatus(PanelStatus.ACTIVE).get(1));
        return shapes;
    }

    @GetMapping("/reset")
    public String resetShape() {
        logger.info("reset SHAPE");
        shapes.clear();
        ledService.clearAllScreens();
        return "redirect:";
    }
}