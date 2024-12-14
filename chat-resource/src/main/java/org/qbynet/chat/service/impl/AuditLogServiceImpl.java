package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.repository.AuditLogRepository;
import org.qbynet.chat.service.AuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    @Resource
    AuditLogRepository auditLogRepository;

    @Override
    public void approveJoinRequest(@NotNull JoinRequest joinRequest, User operator) {
        auditLogRepository.save(AuditLog.builder()
                .operator(operator)
                .targetConversation(joinRequest.getConversation())
                .targetUser(joinRequest.getUser())
                .description("Approved join request")
                .type(AuditType.APPROVE_JOIN_REQUEST).build());
    }

    @Override
    public void joinConversation(@NotNull Member member) {
        auditLogRepository.save(AuditLog.builder()
                .operator(member.getUser())
                .targetConversation(member.getConversation())
                .description("Joined conversation")
                .type(AuditType.JOIN_CONVERSATION).build());
    }

    @Override
    public void memberQuit(@NotNull Member member) {
        auditLogRepository.save(AuditLog.builder()
                .operator(member.getUser())
                .targetConversation(member.getConversation())
                .description("Quit conversation")
                .type(AuditType.QUIT_CONVERSATION)
                .build());
    }

    @Override
    public void memberJoined(@NotNull Member member) {
        auditLogRepository.save(AuditLog.builder()
                .operator(member.getUser())
                .targetConversation(member.getConversation())
                .description("Join conversation")
                .type(AuditType.JOIN_CONVERSATION)
                .build());
    }

    @Override
    public void createConversation(Conversation conversation, User operator) {
        auditLogRepository.save(AuditLog.builder()
                .operator(operator)
                .targetConversation(conversation)
                .description("Create conversation")
                .type(AuditType.CREATE_CONVERSATION)
                .build());
    }

    @Override
    public void configAutoDeleteTimer(@NotNull Conversation conversation, User operator) {
        int timer = conversation.getAutoDeleteTimer();
        auditLogRepository.save(AuditLog.builder()
                .operator(operator)
                .targetConversation(conversation)
                .description((timer != -1) ? "Change auto delete timer to " + timer + "d" : "Cleared auto delete timer")
                .type(AuditType.CHANGE_AUTO_DELETE_TIMER)
                .build());
    }
}
