package org.qbynet.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.ConversationType;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.NotificationPreferment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationMemberVO {
    private ConversationVO conversation;
    private String name;
    private ConversationType type;

    private boolean pinned;
    private boolean archived;
    private NotificationPreferment notificationPreferment;

    public static @NotNull ConversationMemberVO from(@NotNull Member member) {
        ConversationMemberVO vo = new ConversationMemberVO();
        Conversation memberConversation = member.getConversation();
        vo.setConversation(ConversationVO.from(memberConversation));
        vo.setName(memberConversation.getName());
        vo.setType(memberConversation.getType());
        vo.setPinned(member.isPinned());
        vo.setArchived(member.isArchived());
        vo.setNotificationPreferment(member.getNotifications());
        return vo;
    }
}
