package org.qbynet.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.EditProfileDTO;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

public interface UserService {
    User createProfile(String remoteId, String nickname);

    CreateBot createBot(User owner, String username, String nickname) throws JsonProcessingException;

    CreateBot resetBotToken(Bot bot) throws JsonProcessingException;

    User find(String remoteId);

    User find(Principal principal);

    default User find(@NotNull AbstractSubProtocolEvent event) {
        return find(event.getUser());
    }

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

    boolean canAccess(PrivacyPreferment field, @NotNull User target, User operator);

    default boolean canAccessStatus(User target, User operator) {
        return canAccess(target.getPrivacy().getStatus(), target, operator);
    }

    default boolean canAccessOnlineStatus(User target, User operator) {
        return canAccess(target.getPrivacy().getOnlineStatus(), target, operator);
    }

    default boolean canSendCalls(User target, User operator) {
        return canAccess(target.getPrivacy().getCalls(), target, operator);
    }

    boolean hasContact(User owner, User target);

    /**
     * Get user from a request
     */
    User currentUser();

    User editProfile(@NotNull EditProfileDTO input);

    boolean checkUsernameAvailable(String username);

    void goneOffline(@NotNull User user);
}
