package org.qbynet.authorization.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.Instant;
import java.util.*;

@Data
@Document(value = "oauthAuthorization")
@NoArgsConstructor
@AllArgsConstructor
public class JsonAuthorization {
    @Id
    private String id;

    private String registeredClientId;

    private String principalName;

    private AuthorizationGrantType authorizationGrantType;

    private Set<String> authorizedScopes;

    private List<InternalToken> tokens;

    private Map<String, Object> attributes;

    @SuppressWarnings("unchecked")
    public static JsonAuthorization from(OAuth2Authorization authorization) throws NoSuchFieldException, IllegalAccessException {
        JsonAuthorization jsonAuthorization = new JsonAuthorization();
        jsonAuthorization.setId(authorization.getId());
        jsonAuthorization.setRegisteredClientId(authorization.getRegisteredClientId());
        jsonAuthorization.setPrincipalName(authorization.getPrincipalName());
        jsonAuthorization.setAuthorizationGrantType(authorization.getAuthorizationGrantType());
        jsonAuthorization.setAuthorizedScopes(authorization.getAuthorizedScopes());
        // process UsernamePasswordAuthenticationToken
        Map<String, Object> finalMap = new HashMap<>(authorization.getAttributes());
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) finalMap.get(Principal.class.getName());
        finalMap.put(Principal.class.getName(), InternalUsernamePasswordAuthenticationToken.from(usernamePasswordAuthenticationToken));
        jsonAuthorization.setAttributes(finalMap);
        Class<? extends OAuth2Authorization> klass = authorization.getClass();
        Field tokensField = klass.getDeclaredField("tokens");
        tokensField.setAccessible(true);
        Map<Class<? extends OAuth2Token>, OAuth2Authorization.Token<?>> tokens = (Map<Class<? extends OAuth2Token>, OAuth2Authorization.Token<?>>) tokensField.get(authorization);
        List<InternalToken> tokenList = tokens.values().stream().map(it -> new InternalToken(it.getToken(), it.getMetadata())).toList();
        jsonAuthorization.setTokens(tokenList);
        return jsonAuthorization;
    }

    public OAuth2Authorization convent(RegisteredClient registeredClient) {
        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(id)
                .principalName(principalName)
                .authorizationGrantType(authorizationGrantType)
                .authorizedScopes(authorizedScopes)
                .attributes((it) -> it.putAll(attributes));
        tokens.forEach(token -> builder.token(token.conventToOriginToken()));
        return builder.build();
    }

//    @Setter
//    @Getter
//    @SuppressWarnings("unchecked")
//    @EqualsAndHashCode(callSuper = true)
//    public static class InternalOAuth2Authorization extends OAuth2Authorization {
//        public static InternalOAuth2Authorization fromOrigin(OAuth2Authorization source) {
//            // process source with reflect
//            return moveValue(source);
//        }
//
//        @SneakyThrows
//        private static InternalOAuth2Authorization moveValue(OAuth2Authorization source) {
//            InternalOAuth2Authorization target = new InternalOAuth2Authorization();
//            Class<? extends OAuth2Authorization> sourceClass = source.getClass();
//            Class<? extends InternalOAuth2Authorization> targetClass = target.getClass();
//
//            for (Field sourceField : sourceClass.getDeclaredFields()) {
//                sourceField.setAccessible(true);
//                if (sourceField.getName().equals("serialVersionUID")) {
//                    continue;
//                }
//                Object o = sourceField.get(source);
//
//                Field targetField = targetClass.getSuperclass().getDeclaredField(sourceField.getName());
//                targetField.setAccessible(true);
//                targetField.set(target, o);
//            }
//            return target;
//        }
//
//        @Override
//        public <T extends OAuth2Token> Token<T> getToken(Class<T> tokenType) {
//            Class<T> realTokenType = tokenType;
//            if (tokenType.equals(OAuth2AccessToken.class)) {
//                realTokenType = (Class<T>) InternalOAuth2AccessToken.class;
//            } else if (tokenType.equals(OAuth2RefreshToken.class)) {
//                realTokenType = (Class<T>) InternalOAuth2RefreshToken.class;
//            } else if (tokenType.equals(OAuth2AuthorizationCode.class)) {
//                realTokenType = (Class<T>) InternalOAuth2AuthorizationCode.class;
//            } else if (tokenType.equals(OidcIdToken.class)) {
//                realTokenType = (Class<T>) InternalOidcIdToken.class;
//            }
//            return super.getToken(realTokenType);
//        }
//    }

    @Setter
    @EqualsAndHashCode(callSuper = true)
    public static class InternalUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {
        public InternalUsernamePasswordAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
            super(principal, credentials, authorities);
        }

        public static InternalUsernamePasswordAuthenticationToken from(UsernamePasswordAuthenticationToken source) {
            Object principal = source.getPrincipal();
            if (principal instanceof User p1) {
                principal = InternalUser.from(p1);
            }
            InternalUsernamePasswordAuthenticationToken token = new InternalUsernamePasswordAuthenticationToken(principal, source.getCredentials(), source.getAuthorities());
            Object details = source.getDetails();
            // process details
            if (details instanceof WebAuthenticationDetails details1) {
                details = InternalWebAuthenticationDetails.from(details1);
            }
            token.setDetails(details);
            return token;
        }
    }

    @Setter
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class InternalWebAuthenticationDetails extends WebAuthenticationDetails {

        public InternalWebAuthenticationDetails(String remoteAddress, String sessionId) {
            super(remoteAddress, sessionId);
        }

        public static InternalWebAuthenticationDetails from(WebAuthenticationDetails source) {
            return new InternalWebAuthenticationDetails(source.getRemoteAddress(), source.getSessionId());
        }
    }

    @Setter
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class InternalUser extends User {
        public InternalUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
            super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        }

        public static InternalUser from(User principal) {
            String pwd = principal.getPassword();
            if (pwd == null) {
                pwd = "unknown password";
            }
            return new InternalUser(principal.getUsername(), pwd, principal.isEnabled(), principal.isAccountNonExpired(), principal.isCredentialsNonExpired(), principal.isAccountNonLocked(), principal.getAuthorities());
        }
    }

    @Data
    public static class InternalToken {
        private Object token;
        private Map<String, Object> metadata;

        public InternalToken(Object token, Map<String, Object> metadata) {
            if (token instanceof OAuth2RefreshToken refreshToken) {
                token = InternalOAuth2RefreshToken.from(refreshToken);
            } else if (token instanceof OAuth2AccessToken accessToken) {
                token = InternalOAuth2AccessToken.from(accessToken);
            } else if (token instanceof OAuth2AuthorizationCode code) {
                token = InternalOAuth2AuthorizationCode.from(code);
            } else if (token instanceof OidcIdToken oidcIdToken) {
                token = InternalOidcIdToken.from(oidcIdToken);
            }
            this.token = token;
            this.metadata = metadata;
        }

        public OAuth2Token conventToOriginToken() {
            OAuth2Token result = null;
            if (token instanceof InternalOAuth2RefreshToken refreshToken) {
                result = new OAuth2RefreshToken(refreshToken.getTokenValue(), refreshToken.getIssuedAt(), refreshToken.getExpiresAt());
            } else if (token instanceof InternalOAuth2AccessToken accessToken) {
                result = new OAuth2AccessToken(accessToken.getTokenType(), accessToken.getTokenValue(), accessToken.getIssuedAt(), accessToken.getExpiresAt(), accessToken.getScopes());
            } else if (token instanceof InternalOAuth2AuthorizationCode code) {
                result = new OAuth2AuthorizationCode(code.getTokenValue(), code.getIssuedAt(), code.getExpiresAt());
            } else if (token instanceof InternalOidcIdToken oidcIdToken) {
                result = new OidcIdToken(oidcIdToken.getTokenValue(), oidcIdToken.getIssuedAt(), oidcIdToken.getExpiresAt(), oidcIdToken.getClaims());
            }
            return result;
        }
    }

    @Setter
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class InternalOAuth2RefreshToken extends OAuth2RefreshToken {

        public InternalOAuth2RefreshToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
            super(tokenValue, issuedAt, expiresAt);
        }

        public static InternalOAuth2RefreshToken from(OAuth2RefreshToken source) {
            return new InternalOAuth2RefreshToken(source.getTokenValue(), source.getIssuedAt(), source.getExpiresAt());
        }
    }

    @Setter
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class InternalOAuth2AuthorizationCode extends OAuth2AuthorizationCode {

        public InternalOAuth2AuthorizationCode(String tokenValue, Instant issuedAt, Instant expiresAt) {
            super(tokenValue, issuedAt, expiresAt);
        }

        public static InternalOAuth2AuthorizationCode from(OAuth2AuthorizationCode source) {
            return new InternalOAuth2AuthorizationCode(source.getTokenValue(), source.getIssuedAt(), source.getExpiresAt());
        }
    }

    @Setter
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class InternalOAuth2AccessToken extends OAuth2AccessToken {

        public InternalOAuth2AccessToken(TokenType tokenType, String tokenValue, Instant issuedAt, Instant expiresAt, Set<String> scopes) {
            super(tokenType, tokenValue, issuedAt, expiresAt, scopes);
        }

        public static InternalOAuth2AccessToken from(OAuth2AccessToken source) {
            return new InternalOAuth2AccessToken(source.getTokenType(), source.getTokenValue(), source.getIssuedAt(), source.getExpiresAt(), source.getScopes());
        }
    }

    @Setter
    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class InternalOidcIdToken extends OidcIdToken {

        /**
         * Constructs a {@code OidcIdToken} using the provided parameters.
         *
         * @param tokenValue the ID Token value
         * @param issuedAt   the time at which the ID Token was issued {@code (iat)}
         * @param expiresAt  the expiration time {@code (exp)} on or after which the ID Token
         *                   MUST NOT be accepted
         * @param claims     the claims about the authentication of the End-User
         */
        public InternalOidcIdToken(String tokenValue, Instant issuedAt, Instant expiresAt, Map<String, Object> claims) {
            super(tokenValue, issuedAt, expiresAt, claims);
        }

        public static InternalOidcIdToken from(OidcIdToken source) {
            return new InternalOidcIdToken(source.getTokenValue(), source.getIssuedAt(), source.getExpiresAt(), source.getClaims());
        }
    }
}
