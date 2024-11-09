package org.qbynet.authorization.service.impl;

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

@Service
@Log4j2
public class AccountServiceImpl implements AccountService {
    @Resource
    AccountRepository accountRepository;

    @Resource
    PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
        if (accountRepository.count() == 0) {
            Account account = new Account();
            account.setUsername("admin");
            account.setPassword(passwordEncoder.encode("admin")); // todo random password
            account.addRole(Role.ADMIN);
            accountRepository.save(account);
            log.info("Admin account created.");
            log.info("Username: {}", account.getUsername());
            log.info("Password: {}", account.getPassword());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username).orElseGet(() -> accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username)));
        return User.withUsername(username)
                .roles(account.getRoles().stream().map(Enum::name).toList().toArray(new String[0]))
                .password(account.getPassword())
                .accountLocked(account.isLocked())
                .build();
    }

    @Override
    public Account findAccountByUsernameOrEmail(String value) {
        return accountRepository.findByUsername(value).orElseGet(() -> accountRepository.findByEmail(value).orElse(null));
    }

    @Override
    public Account register(String username, String password, String email, boolean isAdmin) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setEmail(email);
        if (isAdmin) {
            account.addRole(Role.ADMIN);
        }
        return accountRepository.save(account);
    }
}
