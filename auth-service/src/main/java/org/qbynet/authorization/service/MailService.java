package org.qbynet.authorization.service;

public interface MailService {
    void sendConfirmToken(String email, String token);
}
