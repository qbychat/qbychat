package org.qbynet.chat.controller;

import graphql.schema.DataFetchingEnvironment;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbychat.graphql.types.GraphQlConversation;
import org.qbychat.graphql.types.GraphQlUser;
import org.qbychat.graphql.types.ServerSettings;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.config.BotConfig;
import org.qbynet.chat.entity.config.TelegramConfig;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class QueryController {
    @Value("${qbychat.user.username.min-length}")
    int usernameMinLength;

    @Resource
    UserService userService;

    @Resource
    ConversationService conversationService;

    @Resource
    TelegramConfig telegramConfig;

    @Resource
    BotConfig botConfig;

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public Mono<GraphQlUser> myself(@NotNull DataFetchingEnvironment dfe) {
        User user = userService.find(dfe);
        assert user != null;
        return Mono.just(user.toGraphQL());
    }

    @QueryMapping
    public Mono<ServerSettings> settings() {
        return Mono.just(ServerSettings.newBuilder()
            .telegram(telegramConfig.isEnabled())
            .bot(botConfig.isEnabled())
            .minUsernameLength(usernameMinLength)
            .build());
    }

    @QueryMapping
    public Mono<GraphQlConversation> conversationById(@Argument String id, @NotNull DataFetchingEnvironment dfe) {
        User user = userService.find(dfe);
        // find member
        return conversationService.findConversation(id)
            .flatMap(conversation -> conversationService
                .findMember(conversation, user)

                .map(Member::conversationToGraphQL)
                .defaultIfEmpty(conversation.toLimitedGraphQl())
            );
    }
}
