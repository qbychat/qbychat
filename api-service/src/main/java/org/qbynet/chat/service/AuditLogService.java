package org.qbynet.chat.service;

import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.JoinRequest;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.User;

public interface AuditLogService {
    void approveJoinRequest(@NotNull JoinRequest joinRequest, User operator);

    void memberQuit(Member member);

    void memberJoined(Member member);

    void createConversation(Conversation conversation, User operator);

    void configAutoDeleteTimer(@NotNull Conversation conversation, User operator);

    void denyJoinRequest(JoinRequest joinRequest, User operator);

    void anonymous(@NotNull Member member, User operator);
}
