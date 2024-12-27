package org.qbynet.chat.entity;

import lombok.Data;
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
//    private SenderVO sender; todo
//    private JoinRequestVO joinRequest;

    private NotificationType type;
}
