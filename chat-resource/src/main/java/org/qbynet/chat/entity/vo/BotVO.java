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
        BotVO vo = new BotVO();
        vo.setId(cb.getBot().getId());
        vo.setUser(UserVO.fromUser(cb.getBot().getBot()));
        vo.setToken(cb.getToken());
        return vo;
    }

    public static @NotNull BotVO from(@NotNull Bot source) {
        BotVO vo = new BotVO();
        vo.setId(source.getId());
        vo.setUser(UserVO.fromUser(source.getBot()));
        return vo;
    }
}
