package com.ats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to map clean user-facing page URLs to static HTML files.
 * This acts as a standard page router using forward paths.
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/apply")
    public String apply() {
        return "forward:/apply.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/candidate-details")
    public String candidateDetails() {
        return "forward:/candidate-details.html";
    }
}
