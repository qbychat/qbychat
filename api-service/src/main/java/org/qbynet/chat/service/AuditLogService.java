package org.qbynet.chat.service;

import org.qbynet.chat.entity.AuditLog;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.User;
import reactor.core.publisher.Mono;

public interface AuditLogService {

    Mono<AuditLog> createConversation(Conversation conversation, User owner);
}
