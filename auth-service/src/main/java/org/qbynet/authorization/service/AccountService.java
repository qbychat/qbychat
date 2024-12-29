package org.qbynet.authorization.service;

import org.qbynet.authorization.entity.Account;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService {

    Account register(String email, String password, boolean isAdmin);

    boolean recordVerify(String email, String password);

    Account doVerify(String email, String token);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasAdmin();

    Account findById(String id);

    Account currentAccount();
}
