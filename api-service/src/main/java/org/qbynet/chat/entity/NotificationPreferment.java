package org.qbynet.chat.entity;

public enum NotificationPreferment {
    EVERYTHING, // push everything to me
    MENTION_AND_REPLY, // only push if somebody mentioned me or replied to me
    NOTHING // push nothing to me
}
