package org.qbynet.chat.service;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;

public interface MediaService {
    Media fromRemote(URI remote);

    void fromRemote(URI remote, Consumer<Media> consumer) throws MalformedURLException;

    Media findById(String id);

    StreamMetadata openInputStream(Media media) throws IOException;

    Media upload(InputStream inputStream, String filename, String contentType, User uploader) throws IOException;

    boolean hasFile(String sha256);

    @NotNull Optional<Media> fromExist(User user, String hash, String name, String contentType);

    @Data
    @Builder
    class StreamMetadata {
        private InputStream inputStream;
        private long size;
    }
}
