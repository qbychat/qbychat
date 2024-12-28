package org.qbynet.chat.service;

import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.ConversationType;
import org.qbynet.chat.entity.User;
import reactor.core.publisher.Mono;

public interface ConversationService {
    Mono<Conversation> create(String name, ConversationType conversationType, User owner);
}
