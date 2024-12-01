package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Media;

@Data
public class ExistMediaVO {
    private boolean exist;

    private String id;
    private String name;
    private String hash;
    private String contentType;

    public static @NotNull ExistMediaVO from(@NotNull Media origin) {
        ExistMediaVO existMedia = new ExistMediaVO();
        existMedia.setExist(true);
        existMedia.setId(origin.getId());
        existMedia.setName(origin.getName());
        existMedia.setHash(origin.getHash());
        existMedia.setContentType(origin.getContentType());
        return existMedia;
    }

    public static @NotNull ExistMediaVO missing(String hash) {
        ExistMediaVO existMedia = new ExistMediaVO();
        existMedia.setExist(false);
        existMedia.setHash(hash);
        return existMedia;
    }
}
