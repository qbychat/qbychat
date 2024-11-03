package org.qbynet.authorization.service;

import org.qbynet.authorization.entity.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService {
    Account findAccountByUsernameOrEmail(String usernameOrEmail);
}
