package org.qbychat.gateway.controller;

import jakarta.annotation.Resource;
import org.qbychat.gateway.util.GatewayAddressConfig;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

@Controller
public class IndexController {
    @Resource
    GatewayAddressConfig gatewayAddressConfig;

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("connect")
    public String qr(Model model, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String baseUrl = uri.getScheme() + "//" + uri.getHost() + ":" + uri.getPort();
        model.addAttribute("api", baseUrl + gatewayAddressConfig.getApi());
        model.addAttribute("auth", gatewayAddressConfig.getAuth());
        return "connect";
    }
}
