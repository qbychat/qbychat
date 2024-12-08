package org.qbynet.authorization.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.entity.Role;
import org.qbynet.authorization.entity.Verify;
import org.qbynet.authorization.repository.AccountRepository;
import org.qbynet.authorization.repository.VerifyRepository;
import org.qbynet.authorization.service.AccountService;
import org.qbynet.authorization.service.MailService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return User.withUsername(account.getId())
                .password(account.getPassword())
                .authorities(account.getRoles().stream().map(it -> new SimpleGrantedAuthority("ROLE_" + it)).toList().toArray(SimpleGrantedAuthority[]::new))
                .accountLocked(account.isLocked())
                .build();
    }

    @Override
    public Account register(String email, String password, boolean isAdmin) {
        Account account = new Account();
        account.setPassword(passwordEncoder.encode(password));
        account.setEmail(email);
        if (isAdmin) {
            account.addRole(Role.ADMIN);
        }
        log.info("Account {} was registered. (roles={})", account.getEmail(), account.getRoles());
        return accountRepository.save(account);
    }

    @Override
    public boolean recordVerify(String email, String password) {
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

    @Override
    public boolean hasAdmin() {
        return accountRepository.existsByRolesContains(Role.ADMIN);
    }

    @Override
    public Account findById(String id) {
        return accountRepository.findById(id).orElse(null);
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
