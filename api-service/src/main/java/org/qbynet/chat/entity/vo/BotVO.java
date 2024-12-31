package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Bot;
import org.qbynet.chat.entity.CreateBot;

@Data
public class BotVO {
    private String id;

    private UserVO user;
    private String token = null;

    public static @NotNull BotVO from(@NotNull CreateBot cb) {
        BotVO vo = ignoreToken(cb);
        vo.setToken(cb.getToken());
        return vo;
    }

    public static @NotNull BotVO ignoreToken(@NotNull CreateBot cb) {
        return ignoreToken(cb.getBot());
    }

    public static @NotNull BotVO ignoreToken(@NotNull Bot source) {
        BotVO vo = new BotVO();
        vo.setId(source.getId());
        vo.setUser(UserVO.from(source.getBot()));
        return vo;
    }
}
