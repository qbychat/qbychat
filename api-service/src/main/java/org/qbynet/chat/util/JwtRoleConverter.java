package org.qbynet.chat.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked"})
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(@NotNull Jwt jwt) {
        List<String> authorities = (List<String>) jwt.getClaims().get("roles");
        List<String> scopes = (List<String>) jwt.getClaims().get("scope");
        if (authorities == null || authorities.isEmpty()) {
            return scopes.stream().map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)).collect(Collectors.toList());
        }
        if (!(scopes == null || scopes.isEmpty())) {
            authorities.addAll(scopes.stream().map(it -> "SCOPE_" + it).toList());
        }
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
