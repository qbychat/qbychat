package org.qbynet.chat.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.EditProfileDTO;
import org.qbynet.chat.repository.BotRepository;
import org.qbynet.chat.repository.ContactRepository;
import org.qbynet.chat.repository.TemporaryRelationRepository;
import org.qbynet.chat.repository.UserRepository;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.EventService;
import org.qbynet.chat.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Log4j2
@Service
public class UserServiceImpl implements UserService {
    @Value("${qbychat.user.temp-relations.expire}")
    int relationExpire;

    @Value("${qbychat.user.username.min-length}")
    int minUsernameLength;

    @Value("${qbychat.user.username.max-length}")
    int maxUsernameLength;

    @Resource
    UserRepository userRepository;

    @Resource
    ContactRepository contactRepository;

    @Resource
    ObjectMapper objectMapper;

    @Resource
    BotRepository botRepository;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    EventService eventService;

    @Resource
    ConversationService conversationService;

    @Resource
    TemporaryRelationRepository temporaryRelationRepository;

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
    public CreateBot resetBotToken(@NotNull Bot bot) throws JsonProcessingException {
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
    public User find(@NotNull Principal principal) {
        return find(principal.getName());
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

    @Override
    public boolean isBot(User user) {
        return botRepository.existsByBot(user);
    }

    @Override
    public Status setUserStatus(@NotNull User user, String text) {
        if (text == null || text.isEmpty()) {
            log.info("Cleared status for user {}", user.getNickname());
            user.setStatus(null);
        } else {
            log.info("Set status for user {} (status: {})", user.getNickname(), text);
            user.setStatus(new Status(text));
        }
        eventService.createEventForRelations(user, EventType.CONTACT_STATUS_CHANGED, user.getStatus());
        return userRepository.save(user).getStatus();
    }

    @Override
    public List<User> collectRelations(@NotNull User user) {
        // collect group members
        List<User> users = new ArrayList<>();
        List<Conversation> joinedConversations = conversationService.list(user);
        // group admins
        joinedConversations.stream().filter(c -> c.getType().equals(ConversationType.GROUP)).forEach(conversation -> users.addAll(conversationService.listMembers(conversation).stream().filter(member -> !member.getUser().equals(user) && member.hasPermissions(MemberPermission.LIST_USERS)).map(Member::getUser).toList()));
        // private chats
        joinedConversations.stream().filter(c -> c.getType().equals(ConversationType.PRIVATE_CHAT)).forEach(conversation -> users.add(conversationService.getPrivateChatMember(conversation, user).getUser()));
        // temp relations
        users.addAll(temporaryRelationRepository.findAllByOwner(user.getId()).stream().map(relation -> userRepository.findById(relation.getTarget()).orElse(null)).filter(Objects::nonNull).toList());
        return users;
    }

    @Override
    public void createTemporaryRelation(@NotNull User owner, @NotNull User relation) {
        Optional<TemporaryRelation> current = temporaryRelationRepository.findByOwnerAndTarget(owner.getId(), relation.getId());
        if (current.isPresent()) {
            // renew expire
            log.info("Renew the temporary relation between {} and {}", owner.getNickname(), relation.getNickname());
            TemporaryRelation temporaryRelation = current.get();
            temporaryRelation.setExpire(Instant.now().plus(relationExpire, ChronoUnit.DAYS).getEpochSecond());
            temporaryRelationRepository.save(temporaryRelation);
            return;
        }
        log.info("Create the temporary relation between {} and {}", owner.getNickname(), relation.getNickname());
        temporaryRelationRepository.save(TemporaryRelation.builder()
            .owner(owner.getId())
            .target(relation.getId())
            .expire(Instant.now().plus(relationExpire, ChronoUnit.DAYS).getEpochSecond())
            .build());
    }

    @Override
    public boolean canAccessStatus(@NotNull User target, User operator) {
        switch (target.getPrivacy().getStatus()) {
            case EVERYONE -> {
                return true;
            }
            case NOBODY -> {
                return false;
            }
            case CONTACTS -> {
                return hasContact(operator, target);
            }
        }
        return false; // unreachable
    }

    @Override
    public boolean hasContact(User owner, User target) {
        return contactRepository.existsByOwnerAndTarget(owner, target);
    }

    @Override
    public User currentUser() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        return (User) requestAttributes.getAttribute("user", RequestAttributes.SCOPE_REQUEST);
    }

    @Override
    public User editProfile(@NotNull EditProfileDTO input) {
        User user = currentUser();
        user.setBio(input.getBio());
        // check username available
        String username = input.getUsername();
        if (checkUsernameAvailable(username)) {
            if (username == null) {
                user.setUsername(null);
            } else {
                user.setUsername(username.toLowerCase());
            }
        } else {
            throw new IllegalArgumentException("Bad username: " + username);
        }
        user.setNickname(input.getNickname());
        return userRepository.save(user);
    }

    @Override
    public boolean checkUsernameAvailable(String username) {
        if (username == null) return true;
        if (username.length() < minUsernameLength) return false;
        if (username.length() > maxUsernameLength) return false;
        return userRepository.existsByUsername(username);
    }
}
