package org.qbynet.chat.service.impl;

import cn.hutool.crypto.SecureUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.repository.MediaRepository;
import org.qbynet.chat.service.MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Service
public class MediaServiceImpl implements MediaService {
    @Resource
    MediaRepository mediaRepository;

    @Resource
    OkHttpClient okHttpClient;

    @Value("${qbychat.request.user-agent}")
    String userAgent;

    @Override
    public Media fromRemote(URI remote) {
        Media media = new Media();
        log.info("Download {}", remote);
        try (Response response = okHttpClient.newCall(new Request.Builder()
                .url(remote.toURL())
                .header("User-Agent", userAgent)
                .build()).execute()) {
            if (response.isSuccessful()) {
                media.setContentType(response.header("Content-Type"));
                // save to local
                assert response.body() != null;
                FileMetadata metadata = saveToLocal(response.body().byteStream());
                String parsedFileName = parseFileName(response.header("Content-Disposition"));
                if (parsedFileName != null) {
                    media.setName(parsedFileName);
                } else {
                    String[] path = remote.getPath().split("/");
                    media.setName(path[path.length - 1]);
                }
                media.setHash(metadata.getHash());
            } else {
                log.info("Failed to download file from {} ({})", remote, response.code());
                return null;
            }
        } catch (Exception e) {
            log.info("Failed to download file from {}", remote, e);
            return null;
        }
        Optional<Media> existFile = mediaRepository.findByHash(media.getHash());
        return existFile.orElseGet(() -> mediaRepository.save(media));
    }

    @Override
    public Media findByHash(String hash) {
        return mediaRepository.findByHash(hash).orElse(null);
    }

    @Override
    public InputStream openInputStream(Media media) throws IOException {
        if (media == null) {
            return null;
        }
        File file = getFileByHash(media.getHash());
        if (!file.exists()) {
            return null;
        }
        return FileUtils.openInputStream(file);
    }

    @Override
    public Media upload(MultipartFile file, User uploader) throws IOException {
        Media media = new Media();
        media.setName(file.getOriginalFilename());
        media.setContentType(file.getContentType());
        media.setUploader(uploader);
        FileMetadata metadata = saveToLocal(file.getInputStream());
        media.setHash(metadata.getHash());
        return mediaRepository.save(media);
    }

    private String parseFileName(String contentDisposition) {
        if (contentDisposition != null) {
            Pattern pattern = Pattern.compile("filename=\"?([^\"]+)\"?");
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * Get the file path with the hash
     *
     * @param hash sha256 hash
     * @return the file
     */
    private File getFileByHash(String hash) {
        return new File(FileUtils.current(), ".qbychat/files/" + hash);
    }

    private @NotNull FileMetadata saveToLocal(InputStream source) throws IOException {
        File tmp = File.createTempFile("qbychat-", "-tmp");
        TeeInputStream inputStream = new TeeInputStream(source, FileUtils.openOutputStream(tmp), true);
        // calculate sha256
        String sha256 = SecureUtil.sha256(inputStream);
        File file = getFileByHash(sha256);
        FileMetadata metadata = new FileMetadata();
        metadata.setHash(sha256);
        metadata.setFile(file);
        if (file.exists()) {
            tmp.delete();
            return metadata;
        }
        // move file
        FileUtils.moveFile(tmp, file);
        // generate metadata
        return metadata;
    }

    @Data
    public static class FileMetadata {
        private File file;
        private String hash;
    }
}
