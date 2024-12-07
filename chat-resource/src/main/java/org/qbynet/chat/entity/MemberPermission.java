package org.qbynet.chat.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum MemberPermission {
    SEND_TEXT_MESSAGE,
    SEND_PHOTOS, SEND_VIDEOS, SEND_STICKERS, SEND_MUSIC, SEND_FILES, SEND_VOICE_MESSAGES, SEND_VIDEO_MESSAGES, SEND_POLLS,
    SEND_MEDIA(SEND_PHOTOS, SEND_VIDEOS, SEND_STICKERS, SEND_MUSIC, SEND_FILES, SEND_VOICE_MESSAGES, SEND_VIDEO_MESSAGES, SEND_POLLS),

    ADD_USERS, // Invite with channel id is always allowed
    PIN_MESSAGES,
    CHANGE_SELF_INFO, // edit details for self
    CHANGE_MEMBER_INFO, // edit details for everyone

    BAN_USERS, // ban, kick and mute
    APPROVE_JOIN_REQUESTS,
    CREATE_INVITE_LINKS,
    DELETE_MESSAGES, // Deleting messages from self is always allowed
    ADD_NEW_ADMINS,
    LIST_USERS,

    MEMBER_DEFAULT(SEND_TEXT_MESSAGE, SEND_MEDIA, ADD_USERS, CHANGE_SELF_INFO, LIST_USERS),
    ADMIN_DEFAULT(MEMBER_DEFAULT, PIN_MESSAGES, CHANGE_MEMBER_INFO, BAN_USERS, APPROVE_JOIN_REQUESTS, CREATE_INVITE_LINKS, DELETE_MESSAGES, ADD_NEW_ADMINS),
    CHANNEL_DEFAULT(),
    PRIVATE_CHAT_DEFAULT(SEND_TEXT_MESSAGE, SEND_MEDIA, DELETE_MESSAGES, LIST_USERS);

    private final MemberPermission[] subPermissions;

    MemberPermission(MemberPermission... subPermissions) {
        this.subPermissions = subPermissions;
    }

    public static List<MemberPermission> calculate(List<MemberPermission> permissions) {
        List<MemberPermission> result = new ArrayList<>();
        for (MemberPermission permission : permissions) {
            result.add(permission);
            if (permission.subPermissions.length > 0 && !result.contains(permission)) {
                result.addAll(calculate(List.of(permission.subPermissions)));
            }
        }
        return result;
    }
}
