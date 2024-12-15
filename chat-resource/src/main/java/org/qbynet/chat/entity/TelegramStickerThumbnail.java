package org.qbynet.chat.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramStickerThumbnail {
    @JsonProperty("file_id")
    private String fileId;
    @JsonProperty("file_unique_id")
    private String fileUniqueId;
    @JsonProperty("file_size")
    private int fileSize;
    private int width;
    private int height;
}
