package org.qbynet.authorization.service;

import org.qbynet.authorization.entity.JsonClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public interface RegisteredClientService extends RegisteredClientRepository {
    JsonClient save(JsonClient jsonClient);
}
