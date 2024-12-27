package org.qbynet.chat.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

import java.util.List;

@SuppressWarnings({"unchecked"})
public class JwtRoleConverter implements Converter<Jwt, Flux<GrantedAuthority>> {
    @Override
    public Flux<GrantedAuthority> convert(@NotNull Jwt jwt) {
        List<String> authorities = (List<String>) jwt.getClaims().get("roles");
        List<String> scopes = (List<String>) jwt.getClaims().get("scope");
        if (authorities == null || authorities.isEmpty()) {
            return Flux.fromStream(scopes.stream().map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)));
        }
        if (!(scopes == null || scopes.isEmpty())) {
            authorities.addAll(scopes.stream().map(it -> "SCOPE_" + it).toList());
        }
        return Flux.fromStream(authorities.stream().map(SimpleGrantedAuthority::new));
    }
}
