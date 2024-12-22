package org.qbynet.authorization.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("favicon.ico")
    public String favicon() {
        // todo favicon
        return "redirect:https://spring.io/favicon-32x32.png";
    }
}
