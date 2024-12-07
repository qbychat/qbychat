package org.qbynet.chat.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.BotKeyAuthentication;
import org.qbynet.chat.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class BotAuthenticationFilter extends OncePerRequestFilter {

    private final String[] scopes;
    private final UserService userService;

    public BotAuthenticationFilter(String[] scopes, UserService userService) {
        this.scopes = scopes;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();

        request.setAttribute("bot", false);
        if (!(context.getAuthentication() != null && context.getAuthentication().isAuthenticated())) {
            String botKey = request.getHeader("X-BOT-KEY");
            if (botKey != null) {
                Bot bot = userService.verifyBotToken(botKey);
                if (bot != null) {
                    Authentication auth = new BotKeyAuthentication(bot, scopes);
                    request.setAttribute("bot", true);
                    auth.setAuthenticated(true);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
