package org.qbynet.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramStickerSet {
    private String name;
    private String title;
    private TelegramStickerThumbnail thumbnail;
    private TelegramStickerThumb thumb;
    @JsonProperty("sticker_type")
    private String sickerType;
    @JsonProperty("contains_masks")
    private boolean containsMasks;
    private List<TelegramSticker> stickers;
}
