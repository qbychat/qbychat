package org.qbynet.authorization.controller;

import org.qbynet.authorization.entity.RestBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
//    @GetMapping("login")
//    public RestBean<String> login() {
//        return RestBean.failure(405, "Method not allowed");
//    }
}
