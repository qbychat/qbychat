package org.qbynet.chat.entity;

import lombok.Data;
import org.qbynet.chat.entity.vo.ConversationVO;
import org.qbynet.chat.entity.vo.MediaVO;
import org.qbynet.chat.entity.vo.MessageVO;
import org.qbynet.chat.entity.vo.UserVO;

@Data
public class SearchResult {
    private ConversationVO conversation; // conversation id
    private UserVO user; // user id
    private MessageVO message; // message id
    private MediaVO media; // media id

    private SearchResultType type;
}
