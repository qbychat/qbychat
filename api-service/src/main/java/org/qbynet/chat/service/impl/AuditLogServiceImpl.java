package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.AuditLog;
import org.qbynet.chat.entity.AuditType;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.repository.AuditLogRepository;
import org.qbynet.chat.service.AuditLogService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    @Resource
    AuditLogRepository auditLogRepository;

    @Override
    public Mono<AuditLog> createConversation(Conversation conversation, User owner) {
        return auditLogRepository.save(AuditLog.builder()
            .operator(owner)
            .targetConversation(conversation)
            .description("Create conversation")
            .type(AuditType.CREATE_CONVERSATION)
            .build());
    }
}
