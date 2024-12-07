package org.qbynet.chat.entity;

import lombok.Builder;
import lombok.Data;
import org.qbynet.chat.entity.vo.*;

@Data
@Builder
public class SearchResult {
    private ConversationVO conversation; // conversation id
    private UserVO user; // user id
    private MessageVO message; // message id
    private MediaVO media; // media id
    private InviteLinkVO inviteLink;

    private SearchResultType type;
}
