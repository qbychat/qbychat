package org.qbynet.chat.service;

import lombok.Builder;
import lombok.Data;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

public interface MediaService {
    Media fromRemote(URI remote) throws MalformedURLException;

    Media findByHash(String hash);

    StreamMetadata openInputStream(Media media) throws IOException;

    Media upload(InputStream inputStream, String filename, String contentType, User uploader) throws IOException;

    boolean hasFile(String sha256);

    @Data
    @Builder
    class StreamMetadata {
        private InputStream inputStream;
        private long size;
    }
}
