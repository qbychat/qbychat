package org.qbynet.chat.entity;

public enum ConversationType {
    GROUP,
    CHANNEL, // only admin can view messages
    PRIVATE_MESSAGE,
    SECRETED_CHAT
}
