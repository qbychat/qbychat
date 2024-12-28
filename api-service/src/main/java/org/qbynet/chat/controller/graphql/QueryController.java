package org.qbynet.chat.controller.graphql;

import org.qbynet.chat.entity.User;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Controller
public class QueryController {
    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public User myself() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        return (User) requestAttributes.getAttribute("user", RequestAttributes.SCOPE_REQUEST);
    }
}
