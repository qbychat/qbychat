package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;
import org.qbynet.chat.entity.User;

@Data
@Builder
public class UserVO {
    private String id;
    private String username;
    private String nickname;
    private String bio;

    private long registerTime;
    private long lastLoginTime;

    public static UserVO from(User user) {
        return UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .registerTime(user.getRegisterTime().toEpochMilli())
            .lastLoginTime((user.getLastLoginTime() != null) ? user.getLastLoginTime().toEpochMilli() : -1L)
            .build();
    }
}
