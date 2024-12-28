package org.qbynet.chat.service;

import org.qbynet.chat.entity.LinkPreview;

import java.net.URI;

public interface LinkPreviewService {
    LinkPreview generateLinkPreview(URI link);

    LinkPreview generateOrGetLinkPreview(URI link);

    LinkPreview getLinkPreviewOrNull(URI link);

    LinkPreview fromText(String content);
}
