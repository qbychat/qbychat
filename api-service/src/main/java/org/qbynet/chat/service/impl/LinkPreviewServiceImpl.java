package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.qbynet.chat.entity.LinkPreview;
import org.qbynet.chat.repository.LinkPreviewRepository;
import org.qbynet.chat.service.LinkPreviewService;
import org.qbynet.chat.service.MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.ContentTypeUtils;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Service
public class LinkPreviewServiceImpl implements LinkPreviewService {
    @Resource
    LinkPreviewRepository linkPreviewRepository;

    @Resource
    OkHttpClient okHttpClient;

    @Resource
    MediaService mediaService;

    @Value("${qbychat.request.user-agent}")
    String userAgent;

    @Value("${qbychat.link.ttl}")
    int linkTtl;

    public static @NotNull List<String> extractLinks(String text) {
        List<String> links = new ArrayList<>();
        String regex = "https?://[\\w-]+(\\.[\\w-]+)+(/\\S*)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            links.add(matcher.group());
        }

        return links;
    }

    private @Nullable Metadata fetchMetadata(URI link) {
        Metadata metadata = new Metadata();
        metadata.setLink(link);
        try (Response response = okHttpClient.newCall(new Request.Builder()
            .get()
            .url(link.toURL())
            .header("User-Agent", userAgent)
            .build()).execute()) {
            assert response.body() != null;
            Document document = Jsoup.parse(response.body().string());
            metadata.setTitle(document.title());
            Element descriptionElement = document.selectFirst("meta[name=description]");
            if (descriptionElement != null) {
                metadata.setDescription(descriptionElement.attr("content"));
            }
            // Open Graph image
            Element metaImage = document.selectFirst("meta[property=og:image]");
            if (metaImage != null) {
                metadata.setImage(URI.create(metaImage.attr("content")));
            }
        } catch (Exception e) {
            log.info("Failed to fetch metadata of url {}", link, e);
            return null;
        }
        return metadata;
    }

    @Override
    public LinkPreview generateLinkPreview(URI link) {
        LinkPreview lp = linkPreviewRepository.findByLink(link.toString()).orElseGet(LinkPreview::new);
        lp.setLink(link.toString());
        log.info("Generate link preview for {}", link);
        try (Response response = okHttpClient.newCall(new Request.Builder()
            .head()
            .url(link.toURL())
            .header("User-Agent", userAgent)
            .build()).execute()) {
            int code = response.code();
            lp.setStatus(code); // response status code
            // metadata
            String contentType = response.header("Content-Type");
            long contentLength = Long.parseLong(Objects.requireNonNullElse(response.header("Content-Length"), "-1"));
            boolean isHtml = ContentTypeUtils.isContentTypeHTML(contentType);
            if (contentType == null || contentLength > 2048_000L || (contentLength == -1 && !isHtml)) {
                // file too big or there's no content type
                return null; // nothing
            } else if (isHtml) {
                // html page
                // load page if its size < 2MB
                Metadata metadata = fetchMetadata(link); // fetch metadata
                if (metadata == null) {
                    return null; // failed to parse html...
                }
                lp.setTitle(metadata.getTitle());
                lp.setDescription(metadata.getDescription());
                // download og image
                if (metadata.getImage() != null) {
                    lp.setImage(mediaService.fromRemote(metadata.getImage()));
                }
            } else if (MediaType.parseMediaType("image/*").isCompatibleWith(MediaType.parseMediaType(contentType))) {
                // image
                String[] path = link.getPath().split("/");
                lp.setTitle(path[path.length - 1]);
                lp.setImage(mediaService.fromRemote(link)); // download the image directly
            } else {
                // No description necessary
                return null;
            }

        } catch (Exception e) {
            log.error("Failed to generate link preview of link {}", lp.getLink(), e);
            return null;
        }
        return linkPreviewRepository.save(lp);
    }

    @Override
    public LinkPreview generateOrGetLinkPreview(URI link) {
        Optional<LinkPreview> lpOptional = linkPreviewRepository.findByLink(link.toString());
        if (lpOptional.isPresent()) {
            LinkPreview lp = lpOptional.get();
            if (Instant.now().isAfter(lp.getTimestamp().plus(linkTtl, ChronoUnit.DAYS))) {
                // expired
                return generateLinkPreview(link);
            }
            return lp;
        }
        return generateLinkPreview(link);
    }

    @Override
    public LinkPreview fromText(String content) {
        // parse the first link
        List<String> links = extractLinks(content);
        if (links.isEmpty()) {
            return null;
        }
        return generateOrGetLinkPreview(URI.create(links.getFirst()));
    }

    @Data
    private static class Metadata {
        private URI link;

        private String title;
        private String description;
        private URI image = null;
    }
}
