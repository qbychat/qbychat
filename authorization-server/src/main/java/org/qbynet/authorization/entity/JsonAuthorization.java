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
@Document
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
        tokens.forEach(token -> builder.token((OAuth2Token) token.token));
        return builder.build();
    }

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
    @SuppressWarnings("unused")
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
