package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.qbynet.chat.entity.LinkPreview;
import org.qbynet.chat.repository.LinkPreviewRepository;
import org.qbynet.chat.service.LinkPreviewService;
import org.springframework.stereotype.Service;

import java.net.URI;

@Log4j2
@Service
public class LinkPreviewServiceImpl implements LinkPreviewService {
    @Resource
    LinkPreviewRepository linkPreviewRepository;

    @Resource
    OkHttpClient okHttpClient;

    @Override
    public LinkPreview generateLinkPreview(URI link) {
        LinkPreview lp = new LinkPreview();
        lp.setLink(link.toString());
        // todo: do fetch
//        try (Response response = okHttpClient.newCall(new Request.Builder()
//                        .head()
//                .build()).execute()) {
//
//        } catch (IOException e) {
//            log.error("Failed to generate link preview of link {}", lp.getLink(), e);
//        }
        return linkPreviewRepository.save(lp);
    }

    @Override
    public LinkPreview generateOrGetLinkPreview(URI link) {
        return null;
    }
}
