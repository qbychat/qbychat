package org.qbynet.chat.service;

import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditMessageDTO;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {
    Message send(Message source);

    Message send(SendMessageDTO dto);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean canSendMessage(Conversation conversation, User user);

    void markAsRead(List<String> messages, User user);

    Message findMessageById(String id);

    Message editMessage(Message message, String content, String sticker, List<String> medias, boolean linkPreview);

    Message editMessage(@NotNull EditMessageDTO dto);

    /**
     * Fetch messages
     *
     * @param conversation the Conversation
     * @param user         the User
     * @param pageable     page
     */
    Page<Message> fetchMessages(Conversation conversation, User user, Pageable pageable);

    /**
     * Fetch messages after a message
     *
     * @param conversation the Conversation
     * @param since        where to start
     * @param user         the User
     * @param pageable     page
     */
    Page<Message> fetchMessages(Conversation conversation, Message since, User user, Pageable pageable);

}
