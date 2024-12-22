package org.qbynet.chat.service;

import org.qbynet.chat.module.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UserGraphqlService {
    List<User> getUsers();
}
