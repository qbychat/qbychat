package org.qbychat.gateway.controller;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

@Controller
public class IndexController {
    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("connect")
    public String qr(Model model, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String baseUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
        model.addAttribute("configEndpoint", baseUrl + "/config/json");
        return "connect";
    }
}
