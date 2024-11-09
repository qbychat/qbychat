package org.qbynet.authorization.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {
    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("register")
    public String register() {
        return "register";
    }

    @PostMapping("register")
    public String confirmRegister(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        // todo
        return "conform-email";
    }

    @GetMapping("logout")
    public String logout(HttpSession session, HttpServletRequest request) throws Exception {
        session.invalidate();
        request.logout();
        return "redirect:/user/login?logout";
    }
}
