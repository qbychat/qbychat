package org.qbynet.chat.entity.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class EditStatusDTO {
    @Nullable
    private Integer status = null;

    private String text = "";
}
