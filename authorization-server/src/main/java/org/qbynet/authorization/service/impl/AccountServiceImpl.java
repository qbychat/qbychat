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
            account.setEmail("admin");
            account.setPassword(passwordEncoder.encode("admin")); // todo random password
            account.addRole(Role.ADMIN);
            accountRepository.save(account);
            log.info("Admin account created.");
            log.info("email: {}", account.getEmail());
            log.info("Password: {}", "admin");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return User.withUsername(account.getId())
                .password(account.getPassword())
                .roles(account.getRoles().stream().map(Enum::name).toList().toArray(new String[0]))
                .accountLocked(account.isLocked())
                .build();
    }

    @Override
    public Account register(String password, String email, boolean isAdmin) {
        Account account = new Account();
        account.setPassword(passwordEncoder.encode(password));
        account.setEmail(email);
        if (isAdmin) {
            account.addRole(Role.ADMIN);
        }
        return accountRepository.save(account);
    }

    @Override
    public boolean recordVerify(String password, String email) {
        if (accountRepository.existsByEmail(email) || verifyRepository.existsByEmail(email)) {
            return false; // exists
        }
        Verify verify = new Verify();
        verify.setEmail(email);
        verify.setPassword(passwordEncoder.encode(password));
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
        account.setEmail(verify.getEmail());
        account.setPassword(verify.getPassword());
        verifyRepository.delete(verify);
        log.info("User {} was registered", account.getEmail());
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
