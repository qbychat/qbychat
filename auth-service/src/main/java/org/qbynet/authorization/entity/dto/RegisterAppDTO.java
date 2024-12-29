package org.qbynet.authorization.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegisterAppDTO {
    private String clientId;
    private String clientName;
    private String clientSecret;

    private List<String> clientAuthenticationMethods;
    private List<String> authorizationGrantTypes;
    private List<String> redirectUris;
    private List<String> postLogoutRedirectUris;
    private List<String> scopes;
}
