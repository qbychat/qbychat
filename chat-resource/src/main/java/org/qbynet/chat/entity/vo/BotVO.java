package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.qbynet.chat.entity.CreateBot;

@Data
public class BotVO {
    private UserVO user;
    private String token = null;

    public static BotVO from(CreateBot cb) {
        BotVO botVO = new BotVO();
        botVO.setUser(UserVO.fromUser(cb.getBot().getBot()));
        botVO.setToken(cb.getToken());
        return botVO;
    }
}
