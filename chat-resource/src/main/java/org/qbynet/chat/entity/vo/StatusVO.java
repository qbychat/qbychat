package org.qbynet.chat.entity.vo;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.qbynet.chat.entity.Status;

@Data
@Builder
public class StatusVO {

    public int status; // 1-Text 2-Playing Games 3-Listening To Music

    @Nullable
    public String text;

    public static StatusVO from(Status status) {
        switch (status.getStatus()) {
            case 0 -> {
                return StatusVO.builder().status(0).build(); // none
            }
            case 1 -> {
                return StatusVO.builder().text(status.getText()).status(status.getStatus()).build(); // text
            }
            case 2 -> {
                return null; // todo: playing games
            }
            case 3 -> {
                return null; // todo: listening to music
            }
            default -> {
                return null;
            }
        }
    }
}
