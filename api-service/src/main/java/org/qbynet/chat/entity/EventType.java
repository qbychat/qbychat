package org.qbynet.chat.entity;

public enum EventType {
    TEST,

    JOIN_CONVERSATION,
    QUIT_CONVERSATION,

    CONTACT_ONLINE,
    CONTACT_OFFLINE,
    USER_STATUS_CHANGED,

    MEMBER_INFO_CHANGED, // todo send this event on a conversation pinned, archived or member nickname changed
    DISBAND_CONVERSATION,

    UPDATE_CONVERSATION_STATUS,

    CLEAR_HISTORY,
    MESSAGE_READ,
    MESSAGE_DELETED
}
