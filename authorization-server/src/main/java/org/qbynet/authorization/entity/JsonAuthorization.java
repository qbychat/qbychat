package org.qbynet.authorization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private List<OAuth2Authorization.Token<?>> tokens;

    private Map<String, Object> attributes;

    @SuppressWarnings("unchecked")
    public static JsonAuthorization from(OAuth2Authorization authorization) throws NoSuchFieldException, IllegalAccessException {
        JsonAuthorization jsonAuthorization = new JsonAuthorization();
        jsonAuthorization.setId(authorization.getId());
        jsonAuthorization.setRegisteredClientId(authorization.getRegisteredClientId());
        jsonAuthorization.setPrincipalName(authorization.getPrincipalName());
        jsonAuthorization.setAuthorizationGrantType(authorization.getAuthorizationGrantType());
        jsonAuthorization.setAuthorizedScopes(authorization.getAuthorizedScopes());
        jsonAuthorization.setAttributes(authorization.getAttributes());
        Class<? extends OAuth2Authorization> klass = authorization.getClass();
        Field tokensField = klass.getDeclaredField("tokens");
        tokensField.setAccessible(true);
        Map<Class<? extends OAuth2Token>, OAuth2Authorization.Token<?>> tokens = (Map<Class<? extends OAuth2Token>, OAuth2Authorization.Token<?>>) tokensField.get(authorization);
        jsonAuthorization.setTokens(tokens.values().stream().toList());
        return jsonAuthorization;
    }

    public OAuth2Authorization convent(RegisteredClient registeredClient) {
        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(id)
                .principalName(principalName)
                .authorizationGrantType(authorizationGrantType)
                .authorizedScopes(authorizedScopes)
                .attributes((it) -> it.putAll(attributes));
        tokens.forEach(token -> builder.token(token.getToken()));
        return builder.build();
    }
}
