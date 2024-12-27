package org.qbynet.chat.service;

import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.User;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface UserService {
    Mono<User> createProfile(String remoteId, String nickname);

    Mono<User> find(String remoteId);

    Mono<User> find(Principal principal);

    Mono<Bot> verifyBotToken(String botKey);

    Mono<User> findByUsername(String username);

}
