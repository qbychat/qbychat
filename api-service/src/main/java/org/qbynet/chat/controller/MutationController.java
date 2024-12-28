package org.qbynet.chat.controller;

import graphql.schema.DataFetchingEnvironment;
import jakarta.annotation.Resource;
import org.qbychat.graphql.types.GraphQlUser;
import org.qbychat.graphql.types.UpdateProfileInput;
import org.qbynet.chat.entity.User;
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

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_profile.edit')")
    public Mono<GraphQlUser> updateProfile(@Argument UpdateProfileInput input, DataFetchingEnvironment dfe) {
        User user = userService.find(dfe);
        Mono<User> userMono = userService.updateProfile(user, input);
        // todo handle errors
        return userMono
            .map(u -> user.toGraphQL());
    }
}
