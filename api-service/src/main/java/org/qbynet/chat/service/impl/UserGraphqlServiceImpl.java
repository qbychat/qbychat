package org.qbynet.chat.service.impl;

import org.qbynet.chat.module.User;
import org.qbynet.chat.service.UserGraphqlService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserGraphqlServiceImpl implements UserGraphqlService {
    private final Map<String, User> userRepository = new HashMap<>();

    public UserGraphqlServiceImpl() {
        userRepository.put("1", new User("1", "1", "zszf", "zszf", "I'm the Key!"));
    }

    @Override
    public List<User> getUsers() {
        return userRepository.values().stream().toList();
    }
}
