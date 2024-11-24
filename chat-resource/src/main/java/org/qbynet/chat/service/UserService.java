package org.qbynet.chat.service;

import org.qbynet.chat.entity.User;

import java.security.Principal;

public interface UserService {
    User createProfile(String remoteId, String nickname);

    User find(String remoteId);

    User find(Principal principal);

    User update(User user);
}
