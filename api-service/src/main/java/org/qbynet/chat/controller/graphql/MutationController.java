package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Status;
import org.qbynet.chat.entity.User;
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
    @Secured("SCOPE_status.edit")
    public Status updateStatus(@Argument String text) {
        User user = userService.currentUser();
        return userService.setUserStatus(user, text);
    }
}
