package org.qbynet.client.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@Controller
public class AuthorizationController {
    @GetMapping(value = "/authorize", params = "grant_type=authorization_code")
    public String authorizationCodeGrant(Model model,
                                         @RegisteredOAuth2AuthorizedClient("messaging-client-authorization-code")
                                         OAuth2AuthorizedClient authorizedClient) {
        log.info(authorizedClient.getAccessToken()); // todo
        return "index";
    }
}
