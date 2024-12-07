package org.qbynet.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.CreateBot;
import org.qbynet.chat.entity.User;

import java.io.IOException;
import java.security.Principal;

public interface UserService {
    User createProfile(String remoteId, String nickname);

    CreateBot createBot(User owner, String username, String nickname) throws JsonProcessingException;

    CreateBot resetBotToken(Bot bot) throws JsonProcessingException;

    User find(String remoteId);

    User find(Principal principal);

    User update(User user);

    Bot verifyBotToken(String botKey) throws IOException;

    User findByUsername(String username);
}
