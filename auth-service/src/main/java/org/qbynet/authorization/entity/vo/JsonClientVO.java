package org.qbynet.authorization.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.authorization.entity.JsonClient;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.time.Instant;
import java.util.List;

@Data
public class JsonClientVO {
    private String id;

    private String clientId;
    private String clientName;
    private Instant clientIdIssuedAt;
    private Instant clientIdExpiresAt;

    private List<String> clientAuthenticationMethods;
    private List<String> authorizationGrantTypes;
    private List<String> redirectUris;
    private List<String> postLogoutRedirectUris;
    private List<String> scopes;

    public static @NotNull JsonClientVO from(@NotNull JsonClient source) {
        JsonClientVO vo = new JsonClientVO();
        vo.setId(source.getId());
        vo.setClientId(source.getClientId());
        vo.setClientName(source.getClientName());
        vo.setClientIdIssuedAt(source.getClientIdIssuedAt());
        vo.setClientIdExpiresAt(source.getClientSecretExpiresAt());
        vo.setClientAuthenticationMethods(source.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::getValue).toList());
        vo.setAuthorizationGrantTypes(source.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).toList());
        vo.setRedirectUris(source.getRedirectUris().stream().toList());
        vo.setPostLogoutRedirectUris(source.getPostLogoutRedirectUris().stream().toList());
        vo.setScopes(source.getScopes().stream().toList());
        return vo;
    }
}
