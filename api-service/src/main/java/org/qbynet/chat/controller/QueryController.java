package org.qbynet.chat.controller;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import org.jetbrains.annotations.NotNull;
import org.qbychat.graphql.types.GraphQlStatus;
import org.qbychat.graphql.types.GraphQlUser;
import org.qbynet.chat.entity.User;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Controller
public class QueryController {
    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public Mono<GraphQlUser> myself(@NotNull DataFetchingEnvironment dfe) {
        GraphQLContext graphQlContext = dfe.getGraphQlContext();
        User user = (User) ((ServerWebExchange) graphQlContext.get(ServerWebExchange.class)).getAttributes().get("user");
        assert user != null;
        GraphQlUser.Builder builder = GraphQlUser.newBuilder();
        if (user.getStatus() != null) {
            builder.status(GraphQlStatus.newBuilder()
                .text(user.getStatus().getText())
                .build());
        }
        if (user.getLastLoginTime() != null) {
            builder.lastLoginTime(ZonedDateTime.ofInstant(user.getLastLoginTime(), ZoneId.systemDefault()).toLocalDate());
        }
        return Mono.just(builder
            .id(user.getId())
            .bio(user.getBio())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .registerTime(ZonedDateTime.ofInstant(user.getRegisterTime(), ZoneId.systemDefault()).toLocalDate())
            .build());
    }
}
