package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document
public class Conversation {
    @Id
    private String id; // http://resource-server/view/c/<id>/id

    private String name;
    private String description;
    private ConversationType type;
    private String link; // http://resource-server/view/c/<link>
    private Instant createdAt = Instant.now();

    private List<MemberPermission> defaultPermissions = List.of(MemberPermission.MEMBER_DEFAULT);
    private boolean memberVerificationNeeded = false;
    private boolean preview = true; // can member view messages without join?
    private boolean noForward = false; // can members forward messages? (include admins)
    private boolean hideMembers = false; // can normal members view the member list?

    @DBRef
    private Member owner = null; // private message and secreted chat have no owner

    public List<MemberPermission> getDefaultPermissions() {
        return MemberPermission.calculate(defaultPermissions);
    }
}
