package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Data
@Document
public class Member implements Serializable {
    @Id
    private String id;

    @DBRef
    private User user;
    @DBRef
    private Conversation conversation;
    private String nickname = null; // default to user's nickname
    private String title = null;

    private List<MemberPermission> permissions = null; // if this set null, the default permissions in the conversation entity will be used

    private Instant joinedAt = Instant.now();
    private Instant muteUntil = null;
    private Instant banUntil = null;
    private boolean owner = false;
    private boolean anonymous = false;
    private boolean quit = false;

    private boolean pinned = false; // does this user pin this conversation

    private NotificationPreferment notifications = null; // should we send notifications to this user?

    public List<MemberPermission> getPermissions() {
        if (owner) {
            return List.of(MemberPermission.values());
        }
        if (permissions == null) {
            return conversation.getDefaultPermissions();
        }
        return MemberPermission.calculate(permissions);
    }

    public String getNickname() {
        return Objects.requireNonNullElseGet(nickname, () -> user.getNickname());
    }

    public boolean shouldPush(@NotNull Message message) {
        NotificationPreferment real = notifications;
        if (real == null) {
            real = user.getNotificationPreferment(conversation.getType());
        }
        if (real == NotificationPreferment.EVERYTHING) return true;
        if (real == NotificationPreferment.NOTHING) return false;
        // judge is mentioned or replied
        Message reply = message.getReply();
        if (reply != null && reply.getSender() != null && reply.getSender().getId().equals(id)) {
            return true;
        }
        return message.getContent().contains("@" + user.getId() + " ") || message.getContent().endsWith("@" + user.getId());
    }

    public boolean hasPermissions(@NotNull MemberPermission... requiredPermissions) {
        if (owner) return true;
        return new HashSet<>(this.getPermissions()).containsAll(List.of(requiredPermissions));
    }
}
