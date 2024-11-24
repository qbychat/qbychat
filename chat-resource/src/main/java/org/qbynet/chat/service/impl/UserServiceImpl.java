package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.repository.UserRepository;
import org.qbynet.chat.service.UserService;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Log4j2
@Service
public class UserServiceImpl implements UserService {
    @Resource
    UserRepository userRepository;

    @Override
    public User createProfile(String remoteId, String nickname) {
        User user = new User();
        user.setRemoteId(remoteId);
        user.setNickname(nickname);
        log.info("Create user with remote id {}, nickname \"{}\"", remoteId, nickname);
        return userRepository.save(user);
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
}
