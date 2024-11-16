package org.qbynet.authorization.service;

import org.qbynet.authorization.entity.Client;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public interface RegisteredClientService extends RegisteredClientRepository {
    Client save(Client client);
}
