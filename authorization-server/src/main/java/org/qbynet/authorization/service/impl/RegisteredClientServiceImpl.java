package org.qbynet.authorization.service.impl;

import jakarta.annotation.Resource;
import org.qbynet.authorization.repository.RegisteredClientObjectRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

@Service
public class RegisteredClientServiceImpl implements RegisteredClientRepository {
    @Resource
    RegisteredClientObjectRepository registeredClientObjectRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        registeredClientObjectRepository.save(registeredClient);
    }

    @Override
    public RegisteredClient findById(String id) {
        return registeredClientObjectRepository.findById(id).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return registeredClientObjectRepository.findByClientId(clientId).orElse(null);
    }
}
