package org.qbynet.chat.filter;


import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.BotKeyAuthentication;
import org.qbynet.chat.entity.config.BotConfig;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.Charset;

public class BotAuthenticationFilter implements WebFilter {

    private final String[] scopes;
    private final UserService userService;
    private final boolean state;

    public BotAuthenticationFilter(@NotNull BotConfig botConfig, UserService userService) {
        this.scopes = botConfig.getScopes();
        state = botConfig.isEnabled();
        this.userService = userService;
    }

    @SneakyThrows
    @Override
    public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        return Mono.just("").publishOn(Schedulers.boundedElastic()).handle((empty, sink) -> {
                SecurityContext context = SecurityContextHolder.getContext();
                exchange.getAttributes().put("bot", false);
                if (!(context.getAuthentication() != null && context.getAuthentication().isAuthenticated())) {
                    String botToken = exchange.getRequest().getHeaders().getFirst("X-BOT-TOKEN");
                    ServerHttpResponse response = exchange.getResponse();
                    if (!state && botToken != null) {
                        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                        sink.error(new IllegalArgumentException("Bots are not allowed"));
                    } else if (botToken != null) {
                        userService.verifyBotToken(botToken)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Bad bot token")))
                            .flatMap(bot -> {
                                Authentication auth = new BotKeyAuthentication(bot, scopes);
                                exchange.getAttributes().put("bot", true);
                                auth.setAuthenticated(true);
                                return Mono.just(auth);
                            })
                            .onErrorComplete((ex) -> {
                                // bad token
                                sink.error(new IllegalArgumentException("Bad bot token", ex));
                                return true;
                            })
                            .subscribe(it -> {
                                // set authentication object
                                SecurityContextHolder.getContext().setAuthentication(it);
                                sink.complete();
                            });
                    }
                }
            })
            .publishOn(Schedulers.boundedElastic())
            .onErrorComplete((ex) -> {
                // handle errors
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                DataBufferFactory dataBufferFactory = response.bufferFactory();
                DataBuffer buffer = dataBufferFactory.wrap(RestBean.failure(400, ex.getMessage()).toJson().getBytes(Charset.defaultCharset()));
                response.writeWith(Mono.just(buffer))
                    .doOnError((error) -> DataBufferUtils.release(buffer)).block();
                return true;
            })
            .then(Mono.defer(() -> {
                if (!exchange.getResponse().isCommitted()) {
                    return chain.filter(exchange);
                }
                return Mono.empty();
            }));
    }
}
