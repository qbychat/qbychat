package org.qbynet.chat.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.qbychat.graphql.types.GraphQlStatus;
import org.qbychat.graphql.types.GraphQlUser;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.security.Principal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Document
@EqualsAndHashCode(callSuper = false)
public class User implements Principal, Serializable {
    @Id
    private String id;
    private String remoteId; // id from OAuth2 Authorization server

    private String username = null; // the name for user details link http://resource-server/view/u/<username>
    private String nickname = ""; // the name for everyone
    private String bio = null; // a short text to describe yourself

    private Instant registerTime = Instant.now();
    private Instant lastLoginTime = null;

    private NotificationPreferment groupNotificationPreferment = NotificationPreferment.MENTION_AND_REPLY;
    private NotificationPreferment channelNotificationPreferment = NotificationPreferment.EVERYTHING;
    private NotificationPreferment pmNotificationPreferment = NotificationPreferment.EVERYTHING;
    private boolean autoArchive = false;

    private Privacy privacy = new Privacy();
    private Status status = null;

    @Override
    public String getName() {
        return id;
    }

    public NotificationPreferment getNotificationPreferment(@NotNull ConversationType type) {
        return switch (type) {
            case GROUP -> groupNotificationPreferment;
            case CHANNEL -> channelNotificationPreferment;
            case PRIVATE_CHAT -> pmNotificationPreferment;
        };
    }

    public GraphQlUser toGraphQL() {
        GraphQlUser.Builder builder = GraphQlUser.newBuilder();
        if (this.getStatus() != null) {
            builder.status(GraphQlStatus.newBuilder()
                .text(this.getStatus().getText())
                .build());
        }
        if (this.getLastLoginTime() != null) {
            builder.lastLoginTime(ZonedDateTime.ofInstant(this.getLastLoginTime(), ZoneId.systemDefault()).toLocalDate());
        }
        return builder
            .id(this.getId())
            .bio(this.getBio())
            .username(this.getUsername())
            .nickname(this.getNickname())
            .registerTime(ZonedDateTime.ofInstant(this.getRegisterTime(), ZoneId.systemDefault()).toLocalDate())
            .build();
    }
}
