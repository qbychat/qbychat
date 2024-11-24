package org.qbynet.chat.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.service.UserService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;

public class UserFilter extends OncePerRequestFilter {
    private final UserService userService;

    public UserFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            filterChain.doFilter(request, response);
            return;
        }
        User user = userService.find(principal);
        if (user == null) {
            // create user from request
            user = userService.createProfile(principal.getName(), "User");
        }
        request.setAttribute("user", user);
        filterChain.doFilter(request, response);
    }
}
