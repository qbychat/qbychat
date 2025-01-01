package org.qbynet.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Data
@RedisHash(timeToLive = 60L)
@NoArgsConstructor
@AllArgsConstructor
public class MemberActivity {
    @Id
    private String id;

    @Indexed
    private String user; // user id
    @Indexed
    private String conversation; // conversation id
    private MemberActivityEnum activity;

    public enum MemberActivityEnum {
        TYPING,
    }
}
