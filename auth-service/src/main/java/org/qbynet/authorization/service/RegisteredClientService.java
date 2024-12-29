package org.qbynet.authorization.service;

import org.jetbrains.annotations.NotNull;
import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.entity.JsonClient;
import org.qbynet.authorization.entity.dto.RegisterAppDTO;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.List;

public interface RegisteredClientService extends RegisteredClientRepository {
    List<JsonClient> findAllByOwner(Account account);

    @NotNull JsonClient register(RegisterAppDTO data, Account account);
}
