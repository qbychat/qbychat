package org.qbynet.chat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbychat.graphql.types.UpdateProfileInput;
import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.BotToken;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.repository.BotRepository;
import org.qbynet.chat.repository.UserRepository;
import org.qbynet.chat.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Base64;

@Log4j2
@Service
public class UserServiceImpl implements UserService {
    @Value("${qbychat.user.temp-relations.expire}")
    int relationExpire;

    @Value("${qbychat.user.username.min-length}")
    int minUsernameLength;

    @Resource
    UserRepository userRepository;

    @Resource
    ObjectMapper objectMapper;

    @Resource
    BotRepository botRepository;

    @Resource
    PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> createProfile(String remoteId, String nickname) {
        User user = new User();
        user.setRemoteId(remoteId);
        user.setNickname(nickname);
        log.info("Create user with remote id {}, nickname \"{}\"", remoteId, nickname);
        return userRepository.save(user);
    }

    @Override
    public Mono<User> find(String remoteId) {
        return userRepository.findByRemoteId(remoteId);
    }

    @Override
    public Mono<User> find(@NotNull Principal principal) {
        return find(principal.getName());
    }

    @Override
    public Mono<Bot> verifyBotToken(String token) {
        BotToken botToken;
        try {
            // decode base64
            byte[] decodedBytes = Base64.getDecoder().decode(token);
            // decode json
            botToken = objectMapper.readValue(decodedBytes, BotToken.class);
        } catch (Exception e) {
            return Mono.error(e);
        }
        // find bot
        Mono<Bot> bot = botRepository.findById(botToken.getBotId());
        return bot.filter(value -> passwordEncoder.matches(botToken.getBotToken(), value.getToken())).switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid token")));
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User find(@NotNull DataFetchingEnvironment dfe) {
        GraphQLContext graphQlContext = dfe.getGraphQlContext();
        return (User) ((ServerWebExchange) graphQlContext.get(ServerWebExchange.class)).getAttributes().get("user");
    }

    @Override
    public Mono<User> updateProfile(@NotNull User user, @NotNull UpdateProfileInput input) {
        String newUsername = input.getUsername();
        if (newUsername != null) {
            newUsername = newUsername.toLowerCase();
            if (newUsername.length() < minUsernameLength) {
                return Mono.error(new IllegalArgumentException("Username too short"));
            }
        }
        user.setUsername(newUsername);
        user.setNickname(input.getNickname());
        user.setBio(input.getBio());
        if (user.getUsername() == null) {
            return userRepository.save(user);
        }
        String finalNewUsername = newUsername;
        return Mono.defer(() -> (user.getUsername().equals(finalNewUsername)) ? Mono.just(false) : userRepository.existsByUsername(finalNewUsername)).flatMap(exists -> {
            if (exists) return Mono.error(new IllegalArgumentException("Username already exists"));
            return userRepository.save(user); // save
        });
    }

}
