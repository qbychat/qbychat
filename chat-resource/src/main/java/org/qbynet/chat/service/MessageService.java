package org.qbynet.chat.service;

import org.qbynet.chat.entity.Message;

public interface MessageService {
    Message send(Message source);
}
