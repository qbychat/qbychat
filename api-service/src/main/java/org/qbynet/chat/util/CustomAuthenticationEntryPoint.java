package org.qbynet.chat.util;

import org.jetbrains.annotations.NotNull;
import org.qbynet.shared.entity.RestBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private final ReactiveUtil reactiveUtil;

    public CustomAuthenticationEntryPoint(ReactiveUtil reactiveUtil) {
        this.reactiveUtil = reactiveUtil;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, @NotNull AuthenticationException ex) {
        return reactiveUtil.withRestBean(exchange, RestBean.failure(401, ex.getMessage()));
    }
}
