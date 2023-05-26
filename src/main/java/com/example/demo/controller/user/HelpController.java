package com.example.demo.controller.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/user/help")
public class HelpController {
    private static final Logger logger = LoggerFactory.getLogger(HelpController.class);


    @GetMapping("")
    public String getDraw(Model model) {
        return "user/help";
    }
}