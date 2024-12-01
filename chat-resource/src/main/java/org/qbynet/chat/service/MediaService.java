package org.qbynet.chat.service;

import org.qbynet.chat.entity.Media;

import java.net.MalformedURLException;
import java.net.URI;

public interface MediaService {
    Media fromRemote(URI remote) throws MalformedURLException;
}
