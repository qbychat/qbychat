package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditProfileDTO;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

@Controller
public class MutationController {
    @Resource
    UserService userService;

    @MutationMapping
    @Secured("SCOPE_profile.edit")
    public User editProfile(@Argument @NotNull EditProfileDTO input) {
        return userService.editProfile(input);
    }
}
