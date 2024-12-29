package org.qbynet.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.CreateBot;
import org.qbynet.chat.entity.Status;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditProfileDTO;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

public interface UserService {
    User createProfile(String remoteId, String nickname);

    CreateBot createBot(User owner, String username, String nickname) throws JsonProcessingException;

    CreateBot resetBotToken(Bot bot) throws JsonProcessingException;

    User find(String remoteId);

    User find(Principal principal);

    Bot verifyBotToken(String botKey) throws IOException;

    User findByUsername(String username);

    List<Bot> listBots(User user);

    boolean canDeleteBot(String botId, User user);

    void deleteBot(String botId);

    User findById(String id);

    Bot findBot(String id);

    boolean isBot(User user);

    Status setUserStatus(User user, String text);

    List<User> collectRelations(@NotNull User user);

    void createTemporaryRelation(User owner, User relation);

    boolean canAccessStatus(User target, User operator);

    boolean hasContact(User owner, User target);

    /**
     * Get user from a request
     */
    User currentUser();

    User editProfile(@NotNull EditProfileDTO input);

    boolean checkUsernameAvailable(String username);
}
