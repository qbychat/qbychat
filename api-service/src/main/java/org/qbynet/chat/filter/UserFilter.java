package org.qbynet.chat.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
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
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            filterChain.doFilter(request, response);
            return;
        }
        // bot
        if ((boolean) request.getAttribute("bot")) {
            User user = userService.findByUsername(principal.getName());
            if (user == null) {
                // did you drop the user document?
                throw new ServletException("User not found, did you drop the user document?");
            }
            request.setAttribute("user", user);
            filterChain.doFilter(request, response);
            return;
        }
        // normal user
        User user = userService.find(principal);
        if (user == null) {
            // create user from request
            user = userService.createProfile(principal.getName(), "User");
        }
        request.setAttribute("user", user);
        filterChain.doFilter(request, response);
    }
}
