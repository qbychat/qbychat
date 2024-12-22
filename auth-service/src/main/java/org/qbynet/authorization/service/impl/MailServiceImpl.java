package org.qbynet.authorization.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.authorization.service.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class MailServiceImpl implements MailService {
    @Resource
    MailSender mailSender;

    @Value("${spring.mail.username}")
    String from;

    @Override
    public void sendConfirmToken(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Verify your qbychat account - " + token);
        message.setText(String.format("The token is %s, if you haven't request this code, please ignore it.\n", token) +
            "DO NOT share this code to others.\n" +
            "Powered by qbychat - https://github.com/qbychat/qbychat");
        log.info("Sending verify token to email {}", email);
        mailSender.send(message);
    }
}
