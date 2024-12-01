package org.qbynet.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Media;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaVO {
    private String id;

    /**
     * File name
     */
    private String name;
    /**
     * Sha256 hash
     */
    private String hash;
    /**
     * Mime type
     */
    private String contentType;

    public static @NotNull MediaVO from(@NotNull Media origin) {
        MediaVO media = new MediaVO();
        media.setId(origin.getId());
        media.setName(origin.getName());
        media.setHash(origin.getHash());
        media.setContentType(origin.getContentType());
        return media;
    }
}
