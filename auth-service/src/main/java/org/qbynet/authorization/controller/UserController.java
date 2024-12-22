package org.qbynet.authorization.controller;

import jakarta.annotation.Resource;
import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserController {
    @Value("${qbychat.user.register.confirm}")
    boolean confirmEmailNeeded;

    @Resource
    AccountService accountService;

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("register")
    public String register(Model model) {
        if (!accountService.hasAdmin()) {
            model.addAttribute("hasAdminAccount", false);
            return "guide";
        }
        return "register";
    }

    @PostMapping("register")
    public String confirmRegister(Model model, @RequestParam String password, @RequestParam String email) {
        if (confirmEmailNeeded) {
            if (accountService.recordVerify(email, password)) {
                return "redirect:/user/confirm?email=" + email;
            }
            return "redirect:/user/register?error";
        }
        accountService.register(email, password, false);
        model.addAttribute("email", email);
        return "register-success";
    }

    @PostMapping("register/admin")
    public String confirmRegisterAdmin(Model model, @RequestParam String email, @RequestParam String password) {
        model.addAttribute("hasAdminAccount", true);
        if (!accountService.hasAdmin()) {
            accountService.register(email, password, true);
            return "guide";
        }
        return "redirect:/user/register";
    }

    @GetMapping("confirm")
    public String confirm(Model model) {
        model.addAttribute("hasError", false);
        return "confirm-email";
    }

    @PostMapping("confirm")
    public String confirm(@RequestParam String email, @RequestParam String token, Model model) {
        Account account = accountService.doVerify(email, token);
        if (account == null) {
            model.addAttribute("hasError", true);
            return "confirm-email";
        }
        model.addAttribute("email", email);
        return "register-success";
    }
}
