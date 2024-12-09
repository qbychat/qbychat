package org.qbynet.chat.service;

import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.SendMessageDTO;

import java.util.List;

public interface MessageService {
    Message send(Message source);

    Message send(SendMessageDTO dto, User user);

    boolean canSendMessage(Conversation conversation, User user);

    Conversation findConversationById(String id);

    void markAsRead(List<String> messages, User user);
}
