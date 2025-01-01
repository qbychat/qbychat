package org.qbynet.chat.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Data
public class ConversationStatus {
    private String conversation; // conversation id
    private int memberCount;
    private int onlineMemberCount;
    private Duration autoDeleteTimer;
    private List<MemberActivity> activities;

    // === pm ===
    private boolean isPrivateChat;
    private boolean isPartnerOnline;
    private Instant partnerLastOnline;
    private Status partnerStatus;

    /**
     * Create ConversationStatus object for non-pm conversations
     */
    public static @NotNull ConversationStatus create(@NotNull Conversation conversation, int memberCount, int onlineMemberCount, List<MemberActivity> activities) {
        return create(conversation, memberCount, onlineMemberCount, false, null, null, activities);
    }

    /**
     * Create a full ConversationStatus object
     *
     * @param conversation        the Conversation
     * @param memberCount         joined members
     * @param onlineMemberCount   online members count
     * @param pmPartnerOnline     is pm partner online
     * @param pmPartnerStatus     the status of the partner user
     * @param activities          current activities
     * @param pmPartnerLastOnline pm partner last online time
     * @return the ConversationStatus object
     */
    public static @NotNull ConversationStatus create(@NotNull Conversation conversation, int memberCount, int onlineMemberCount, boolean pmPartnerOnline, Instant pmPartnerLastOnline, Status pmPartnerStatus, List<MemberActivity> activities) {
        ConversationStatus status = new ConversationStatus();
        status.setConversation(conversation.getId());
        status.setMemberCount(memberCount);
        status.setOnlineMemberCount(onlineMemberCount);
        status.setAutoDeleteTimer(conversation.getAutoDeleteTimer());
        status.setActivities(activities);
        status.setPrivateChat(conversation.getType() == ConversationType.PRIVATE_CHAT);
        if (status.isPrivateChat()) {
            status.setPartnerOnline(pmPartnerOnline);
            status.setPartnerStatus(pmPartnerStatus);
            status.setPartnerLastOnline(pmPartnerLastOnline);
        }
        return status;
    }
}
