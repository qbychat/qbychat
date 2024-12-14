package org.qbynet.chat.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.BotToken;
import org.qbynet.chat.entity.CreateBot;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.repository.BotRepository;
import org.qbynet.chat.repository.UserRepository;
import org.qbynet.chat.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class UserServiceImpl implements UserService {
    @Resource
    UserRepository userRepository;

    @Resource
    ObjectMapper objectMapper;

    @Resource
    BotRepository botRepository;

    @Resource
    PasswordEncoder passwordEncoder;

    @Override
    public User createProfile(String remoteId, String nickname) {
        User user = new User();
        user.setRemoteId(remoteId);
        user.setNickname(nickname);
        log.info("Create user with remote id {}, nickname \"{}\"", remoteId, nickname);
        return userRepository.save(user);
    }

    @Override
    public CreateBot createBot(User owner, String username, String nickname) throws JsonProcessingException {
        if (botRepository.existsByBot(owner)) {
            throw new IllegalArgumentException("Bot cannot create bots.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username " + username + " already exists");
        }
        Bot bot = new Bot();
        bot.setOwner(owner);
        User botUser = new User();
        botUser.setUsername(username);
        botUser.setNickname(nickname);
        bot.setBot(userRepository.save(botUser));
        return resetBotToken(bot); // set token
    }

    @Override
    public CreateBot resetBotToken(Bot bot) throws JsonProcessingException {
        String token = RandomUtil.randomString(20);
        bot.setToken(passwordEncoder.encode(token));

        BotToken botToken = new BotToken();
        Bot savedBot = botRepository.save(bot);
        botToken.setBotId(savedBot.getId());
        botToken.setBotToken(token);
        return CreateBot.builder()
                .bot(savedBot)
                .token(Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(botToken)))
                .build();
    }

    @Override
    public User find(String remoteId) {
        return userRepository.findByRemoteId(remoteId).orElse(null);
    }

    @Override
    public User find(Principal principal) {
        return find(principal.getName());
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public Bot verifyBotToken(String token) throws IOException {
        // decode json
        byte[] decodedBytes = Base64.getDecoder().decode(token);
        BotToken botToken = objectMapper.readValue(decodedBytes, BotToken.class);
        // find bot
        Optional<Bot> bot = botRepository.findById(botToken.getBotId());
        return bot.filter(value -> passwordEncoder.matches(botToken.getBotToken(), value.getToken())).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public List<Bot> listBots(User user) {
        return botRepository.findAllByOwner(user);
    }

    @Override
    public boolean canDeleteBot(String botId, User user) {
        return botRepository.findById(botId).map(it -> it.getOwner().getId().equals(user.getId())).orElse(false);
    }

    @Override
    public void deleteBot(String botId) {
        Bot bot = botRepository.findById(botId).orElse(null);
        if (bot == null) {
            return;
        }
        // make its user anonymous
        log.info("Bot {} was deleted", bot.getBot().getNickname());
        User botUser = bot.getBot();
        botUser.setNickname("Deleted Account");
        botUser.setBio(null);
        botUser.setUsername(null);
        botUser.setLastLoginTime(null);
        userRepository.save(botUser);
        botRepository.delete(bot);
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public Bot findBot(String id) {
        return botRepository.findById(id).orElse(null);
    }
}
