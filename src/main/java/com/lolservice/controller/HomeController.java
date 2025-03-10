package com.lolservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping(value = {"/", "/{path:^(?!api|static|.*\\.).+}/**"})
    public String forward() {
        return "forward:/index.html";
    }

    @GetMapping(value = {"/{path:[^\\.]*}"})
    public String redirect() {
        return "forward:/";
    }
} 