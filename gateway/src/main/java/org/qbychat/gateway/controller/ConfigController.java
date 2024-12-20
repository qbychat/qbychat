package org.qbychat.gateway.controller;

import jakarta.annotation.Resource;
import org.qbychat.gateway.entity.ServiceConfig;
import org.qbychat.gateway.util.GatewayAddressConfig;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

@RestController
@RequestMapping("/config")
public class ConfigController {
    @Resource
    GatewayAddressConfig gatewayAddressConfig;

    @GetMapping("json")
    public ServiceConfig json(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String baseUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
        return ServiceConfig.builder()
            .api(baseUrl + gatewayAddressConfig.getApi())
            .auth(gatewayAddressConfig.getAuth())
            .build();
    }
}
