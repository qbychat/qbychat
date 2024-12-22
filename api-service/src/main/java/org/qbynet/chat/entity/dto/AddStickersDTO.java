package org.qbynet.chat.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddStickersDTO {
    /**
     * Sticker pack id
     */
    private String pack;
    /**
     * Sticker list
     */
    private List<StickerDTO> stickers;

    @Data
    public static class StickerDTO {
        private String emoji;
        private String media;
    }
}
