package org.qbynet.chat.util;

import org.jetbrains.annotations.NotNull;
import org.qbynet.shared.entity.RestBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {
    private final ReactiveUtil reactiveUtil;

    public CustomAccessDeniedHandler(ReactiveUtil reactiveUtil) {
        this.reactiveUtil = reactiveUtil;
    }

    public Mono<Void> handle(ServerWebExchange exchange, @NotNull AccessDeniedException ex) {
        return reactiveUtil.withRestBean(exchange, RestBean.failure(403, ex.getMessage()));
    }
}
