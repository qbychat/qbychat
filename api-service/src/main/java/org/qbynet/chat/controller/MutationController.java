package org.qbynet.chat.controller;

import graphql.schema.DataFetchingEnvironment;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbychat.graphql.types.GraphQlConversation;
import org.qbychat.graphql.types.GraphQlConversationType;
import org.qbychat.graphql.types.GraphQlUser;
import org.qbychat.graphql.types.UpdateProfileInput;
import org.qbynet.chat.entity.ConversationType;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class MutationController {
    @Resource
    UserService userService;

    @Resource
    ConversationService conversationService;

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_profile.edit')")
    public Mono<GraphQlUser> updateProfile(DataFetchingEnvironment dfe, @Argument UpdateProfileInput input) {
        User user = userService.find(dfe);
        Mono<User> userMono = userService.updateProfile(user, input);
        // todo handle errors
        return userMono
            .map(User::toGraphQL);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_conversation.create')")
    public Mono<GraphQlConversation> createConversation(DataFetchingEnvironment dfe, @Argument String name, @Argument @NotNull GraphQlConversationType type) {
        User user = userService.find(dfe);
        return conversationService.create(name, ConversationType.valueOf(type.name()), user)
            .flatMap(conversation ->
                conversationService.findMember(conversation, user)
                    .map(Member::conversationToGraphQL)
            );
    }
}
