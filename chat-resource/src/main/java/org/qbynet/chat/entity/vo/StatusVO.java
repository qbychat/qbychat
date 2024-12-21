package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.qbynet.chat.entity.User;

@Data
public class StatusVO {
    public String text;

    public static @Nullable StatusVO from(@NotNull User user) {
        if (user.getStatus() == null) return null;
        StatusVO vo = new StatusVO();
        vo.setText(user.getStatus().getText());
        return vo;
    }
}
