package org.qbynet.chat.service;

import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

public interface MediaService {
    Media fromRemote(URI remote) throws MalformedURLException;

    Media findByHash(String hash);

    InputStream openInputStream(Media media) throws IOException;

    Media upload(MultipartFile file, User uploader) throws IOException;
}
