package org.qbynet.chat.filter;

import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.service.UserService;
import org.qbynet.chat.util.ReactiveUtil;
import org.qbynet.shared.entity.RestBean;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class UserFilter implements WebFilter {
    private final UserService userService;
    private final ReactiveUtil reactiveUtil;

    public UserFilter(UserService userService, ReactiveUtil reactiveUtil) {
        this.userService = userService;
        this.reactiveUtil = reactiveUtil;
    }

    @Override
    public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        return exchange.getPrincipal().publishOn(Schedulers.boundedElastic()).handle((principal, sink) -> {
                if (principal == null) {
                    return;
                }
                if (Boolean.TRUE.equals(exchange.getAttribute("bot"))) {
                    // resolve bots
                    Mono<User> userMono = userService.findByUsername(principal.getName())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found, did you drop the user document?")))
                        .doOnError(sink::error);
                    exchange.getAttributes().put("user", userMono.block());
                    return;
                }
                exchange.getAttributes().put("user", userService.find(principal)
                    .switchIfEmpty(Mono.defer(() -> userService.createProfile(principal.getName(), "User")))
                    .block()
                );
            })
            .onErrorComplete(ex -> {
                    // handle errors
                    reactiveUtil.withRestBean(exchange, RestBean.failure(400, ex.getMessage())).block();
                    return true;
                }
            )
            .then(Mono.defer(() -> {
                if (!exchange.getResponse().isCommitted()) {
                    return chain.filter(exchange);
                }
                return Mono.empty();
            }));
    }
}
