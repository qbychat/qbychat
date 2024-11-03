package org.qbynet.authorization.service.impl;

import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.entity.Role;
import org.qbynet.authorization.repository.AccountRepository;
import org.qbynet.authorization.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    AccountRepository accountRepository;

    @Resource
    PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
        if (accountRepository.count() == 0) {
            String password = RandomUtil.randomString(16);
            Account admin = createDefaultAdmin(password);
            log.warn("Default admin account created: {}", admin.getUsername());
            log.warn("Default admin account password: {}", password);
        }
    }

    private Account createDefaultAdmin(String password) {
        Account account = new Account();
        account.setUsername("admin");
        account.setPassword(passwordEncoder.encode(password));
        account.setRoles(List.of(Role.ADMIN));
        return accountRepository.save(account);
    }

    @Override
    public Account findAccountByUsernameOrEmail(String usernameOrEmail) {
        return accountRepository.findByUsername(usernameOrEmail).orElseGet(() -> accountRepository.findByEmail(usernameOrEmail).orElse(null));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = findAccountByUsernameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException(username);
        }
        return User.withUserDetails(account)
                .build();
    }
}
