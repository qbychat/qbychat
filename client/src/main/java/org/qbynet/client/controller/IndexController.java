package org.qbynet.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("logged-out")
    public String loggedOut() {
        return "logged-out";
    }
}
