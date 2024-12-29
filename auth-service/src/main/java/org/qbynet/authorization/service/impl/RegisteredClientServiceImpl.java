package org.qbynet.authorization.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.authorization.entity.JsonClient;
import org.qbynet.authorization.repository.JsonClientRepository;
import org.qbynet.authorization.service.RegisteredClientService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class RegisteredClientServiceImpl implements RegisteredClientService {
    @Resource
    JsonClientRepository jsonClientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        jsonClientRepository.save(JsonClient.from(registeredClient));
    }

    @Override
    public RegisteredClient findById(String id) {
        return jsonClientRepository.findById(id).map(JsonClient::asRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return jsonClientRepository.findByClientId(clientId).map(JsonClient::asRegisteredClient).orElse(null);
    }

}
