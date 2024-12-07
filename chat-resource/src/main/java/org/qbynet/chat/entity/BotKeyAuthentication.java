package org.qbynet.chat.entity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;

public class BotKeyAuthentication implements Authentication {

    private final String[] scopes;
    private final Bot bot;

    private boolean authenticated;

    public BotKeyAuthentication(Bot bot, String[] scopes) {
        this.bot = bot;
        this.scopes = scopes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(scopes).map(it -> new SimpleGrantedAuthority("SCOPE_" + it)).toList();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return bot.getBot();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return bot.getBot().getUsername();
    }
}
