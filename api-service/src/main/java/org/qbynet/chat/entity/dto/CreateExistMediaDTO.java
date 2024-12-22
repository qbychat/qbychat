package org.qbynet.chat.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateExistMediaDTO {
    List<MediaInfo> medias;

    @Data
    public static class MediaInfo {
        private String name;
        private String hash;
        private String contentType;
    }
}
