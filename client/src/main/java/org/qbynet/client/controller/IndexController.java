package org.qbynet.client.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class IndexController {
    @GetMapping("logged-out")
    public String loggedOut() {
        return "logged-out";
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        model.addAttribute("authentication", authentication);
        model.addAttribute("access_token", "unknown");
        return "index";
    }
}
