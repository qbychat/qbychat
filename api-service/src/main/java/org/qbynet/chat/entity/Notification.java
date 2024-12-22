package org.qbynet.chat.entity;

import lombok.Data;
import org.qbynet.chat.entity.vo.JoinRequestVO;
import org.qbynet.chat.entity.vo.SenderVO;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Data
@RedisHash(timeToLive = 3600L)
public class Notification {
    @Id
    @Indexed
    private String id;

    private String content;
    private SenderVO sender;
    private JoinRequestVO joinRequest;

    private NotificationType type;
}
