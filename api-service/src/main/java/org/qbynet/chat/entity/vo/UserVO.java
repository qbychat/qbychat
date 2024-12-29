package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.User;

import java.time.Instant;

@Data
@Builder
public class UserVO {
    private String id;
    private String username;
    private String nickname;
    private String bio;

    private Instant registerTime;
    private Instant lastLoginTime;

    public static UserVO from(@NotNull User user) {
        return UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .registerTime(user.getRegisterTime())
            .lastLoginTime(user.getLastLoginTime())
            .build();
    }
}
