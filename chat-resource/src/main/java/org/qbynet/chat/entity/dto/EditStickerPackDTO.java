package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class EditStickerPackDTO {
    private String pack;
    private String title; // new title
    private String name; // new name
}
