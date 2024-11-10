package org.qbynet.authorization.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.entity.Role;
import org.qbynet.authorization.entity.Verify;
import org.qbynet.authorization.repository.AccountRepository;
import org.qbynet.authorization.repository.VerifyRepository;
import org.qbynet.authorization.service.AccountService;
import org.qbynet.authorization.service.MailService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Log4j2
@Service
public class AccountServiceImpl implements AccountService {
    @Resource
    MailService mailService;

    @Resource
    AccountRepository accountRepository;

    @Resource
    VerifyRepository verifyRepository;

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
            log.info("Password: {}", "admin");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username).orElseGet(() -> accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username)));
        return User.withUserDetails(account)
                .roles(account.getRoles().stream().map(Enum::name).toList().toArray(new String[0]))
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

    @Override
    public boolean recordVerify(String username, String password, String email) {
        if (accountRepository.existsByEmailOrUsername(email, username) || verifyRepository.existsByEmailOrUsername(email, username)) {
            return false; // exists
        }
        Verify verify = new Verify();
        verify.setUsername(username);
        verify.setPassword(passwordEncoder.encode(password));
        verify.setEmail(email);
        String token = generateVerificationCode();
        verify.setToken(token);
        log.info("Verification to {} is created", verify.getEmail());
        verifyRepository.save(verify);
        mailService.sendConfirmToken(email, token);
        return true;
    }

    @Override
    public Account doVerify(String email, String token) {
        Verify verify = verifyRepository.findByEmailAndToken(email, token).orElse(null);
        if (verify == null) {
            return null;
        }
        Account account = new Account();
        account.setUsername(verify.getUsername());
        account.setEmail(verify.getEmail());
        account.setPassword(verify.getPassword());
        verifyRepository.delete(verify);
        log.info("User {} was registered", account.getUsername());
        return accountRepository.save(account);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }
}
