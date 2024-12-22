package org.qbynet.chat.entity.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramSticker {
    private int width;
    private int height;
    private String emoji;
    @JsonProperty("set_name")
    private String setName;
    @JsonProperty("is_animated")
    private boolean isAnimated;
    @JsonProperty("is_video")
    private boolean isVideo;
    private String type;
    @JsonProperty("file_id")
    private String fileId;
    @JsonProperty("file_unique_id")
    private String fileUniqueId;
    @JsonProperty("file_size")
    private int fileSize;
    private TelegramStickerThumbnail thumbnail;
    private TelegramStickerThumb thumb;
}
