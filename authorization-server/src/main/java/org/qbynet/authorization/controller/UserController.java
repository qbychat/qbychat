package org.qbynet.authorization.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public String register() {
        return "register";
    }

    @PostMapping("register")
    public String confirmRegister(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        if (confirmEmailNeeded) {
            if (accountService.recordVerify(username, password, email)) {
                return "redirect:/user/confirm?email=" + email;
            }
            return "redirect:/user/register?error";
        }
        return "register-success";
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
        return "register-success";
    }

    @GetMapping("logout")
    public String logout(HttpSession session, HttpServletRequest request) throws Exception {
        session.invalidate();
        request.logout();
        return "redirect:/user/login?logout";
    }
}
