package com.opencv.ama.demo;

import com.opencv.ama.core.engine.AmaEngine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the four demo pages. The real work happens through the starter's REST controllers. */
@Controller
public class PageController {

    private final AmaEngine engine;

    public PageController(AmaEngine engine) {
        this.engine = engine;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/health")
    public String health(Model model) {
        model.addAttribute("providers", engine.providerHealth());
        return "health";
    }
}