package org.qbynet.chat.service;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.InviteDTO;

import java.util.List;

public interface ConversationService {
    /**
     * Create a conversation
     *
     * @param name the name of the conversation
     * @param type conversation type
     * @param owner the owner
     * @return the Conversation object
     */
    Conversation create(String name, ConversationType type, User owner);

    /**
     * Add the user to the conversation
     *
     * @param conversation the conversation
     * @param user         the member
     * @return the member object
     */
    Member addMember(Conversation conversation, User user);

    void removeMember(Member member);

    void setAnonymous(boolean state, Member member);

    Conversation findByLink(String link);

    JoinConversationDetails join(Conversation conversation, User user);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean canApproveJoinRequest(Member member);

    Member approveJoinRequest(JoinRequest request);

    boolean isBanned(Conversation conversation, User user);

    Conversation findConversationById(String conversation);

    Member findMember(Conversation conversation, User user);

    JoinRequest findJoinRequest(String id);

    List<Member> list(User user);

    List<Member> listMembers(Conversation conversation);

    InviteLink invite(Conversation conversation, User user);

    boolean hasJoined(Conversation conversation, User user);

    /**
     * Toggle the state of auto delete timer
     *
     * @param conversation the conversation
     * @param duration     the timer. set to -1 to disable the timer (Unit: days)
     * @return modified conversation
     */
    @NotNull Conversation switchAutoDeleteTimer(Conversation conversation, int duration, User operator);

    List<Member> listMembersWithPermissions(Conversation conversation, MemberPermission... permissions);

    List<JoinRequest> findAllJoinRequests(Conversation conversation);

    JoinRequest findJoinRequest(Conversation conversation, User user);

    int countJoinRequests(Conversation conversation);

    Member getPrivateChatMember(Conversation conversation, User self);

    InviteLink invite(@NotNull InviteDTO input);

    void denyJoinRequest(JoinRequest joinRequest);

    boolean hasViewPermission(Conversation conversation, User user);

    @Data
    @Builder
    class JoinConversationDetails {
        private boolean joined;
        private boolean banned;
        private Conversation conversation;
        private JoinRequest joinRequest;
    }
}
