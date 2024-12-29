package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.qbynet.chat.annotation.Authorized;
import org.qbynet.chat.entity.Status;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditProfileDTO;
import org.qbynet.chat.entity.vo.UserVO;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.exception.Forbidden;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

@Controller
public class UserController {
    @Resource
    UserService userService;

    @Authorized
    @QueryMapping
    public UserVO myself() {
        return UserVO.from(userService.currentUser());
    }

    @MutationMapping
    @Secured("SCOPE_profile.edit")
    public User editProfile(@Argument @NotNull EditProfileDTO input) {
        return userService.editProfile(input);
    }

    @MutationMapping
    @Secured("SCOPE_status.edit")
    public Status updateStatus(@Argument String text) {
        User user = userService.currentUser();
        return userService.setUserStatus(user, text);
    }

    @QueryMapping
    @Secured("SCOPE_status.read")
    public Status status(@Argument @Nullable String user) {
        User self = userService.currentUser();
        if (user == null) {
            return self.getStatus();
        }
        // find user
        User user1 = userService.findById(user);
        if (user1 != null && userService.canAccessStatus(user1, self)) {
            return user1.getStatus();
        }
        throw new Forbidden("Forbidden");
    }
}
