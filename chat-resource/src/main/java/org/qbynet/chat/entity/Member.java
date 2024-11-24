package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document
public class Member {
    @Id
    private String id;

    @DBRef
    private User user;
    @DBRef
    private Conversation conversation;
    private String nickname;
    private String title = null;

    private List<MemberPermission> permissions = null; // if this set null, the default permissions in the conversation entity will be used

    private Instant joinedAt = Instant.now();
    private Instant muteUntil = null;
    private Instant banUntil = null;
    private boolean owner = false;
    private boolean anonymous = false;
    private boolean verified = !conversation.isMemberVerificationNeeded();
    private boolean quit = false;

    private boolean notifications = true; // should we send notifications to this user?

    public List<MemberPermission> getPermissions() {
        if (owner) {
            return List.of(MemberPermission.values());
        }
        if (!verified) {
            return List.of();
        }
        if (permissions == null) {
            return conversation.getDefaultPermissions();
        }
        return MemberPermission.calculate(permissions);
    }
}
