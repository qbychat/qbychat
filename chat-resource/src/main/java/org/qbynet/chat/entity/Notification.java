package org.qbynet.chat.entity;

import lombok.Data;
import org.qbynet.chat.entity.vo.SenderVO;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@Data
@RedisHash(timeToLive = 3600L)
public class Notification {
    @Id
    @Indexed
    private String id;

    private String content;
    private SenderVO sender;

    private NotificationType type;

    private List<String> destinations = List.of();
}