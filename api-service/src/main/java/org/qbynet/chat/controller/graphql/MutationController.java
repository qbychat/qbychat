package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Status;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Controller
public class MutationController {

    @Resource
    UserService userService;

    @MutationMapping
    @Secured("SCOPE_status.edit")
    public Status updateStatus(@Argument String text) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        userService.setUserStatus((User)requestAttributes.getAttribute("user", RequestAttributes.SCOPE_REQUEST), text);
        return userService.getUserStatus((User)requestAttributes.getAttribute("user", RequestAttributes.SCOPE_REQUEST));
    }
}
