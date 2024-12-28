package org.qbynet.chat.exception;

public class BadRequest extends RuntimeException {
    public BadRequest(String message) {
        super(message);
    }
}
