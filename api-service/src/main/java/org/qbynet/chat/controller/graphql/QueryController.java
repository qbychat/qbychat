package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

@Controller
public class QueryController {
    @Resource
    UserService userService;

    @QueryMapping
    @Secured("ROLE_USER")
    public User myself() {
        return userService.currentUser();
    }
}
