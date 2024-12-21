package org.qbynet.chat.service.impl;

import cn.hutool.crypto.SecureUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.repository.MediaRepository;
import org.qbynet.chat.service.MediaService;
import org.qbynet.chat.util.CompressUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Log4j2
@Service
public class MediaServiceImpl implements MediaService {
    @Resource
    MediaRepository mediaRepository;

    @Resource
    OkHttpClient okHttpClient;

    @Resource
    CompressUtil compressUtil;

    @Value("${qbychat.request.user-agent}")
    String userAgent;

    @Override
    public Media fromRemote(URI remote) {
        log.info("Download {}", remote);
        Media media;
        try (Response response = okHttpClient.newCall(new Request.Builder()
            .url(remote.toURL())
            .header("User-Agent", userAgent)
            .build()).execute()) {
            media = saveMedia0(remote, response);
            if (media == null) {
                throw new RuntimeException("Failed to save media");
            }
        } catch (Exception e) {
            log.info("Failed to download file from {}", remote, e);
            return null;
        }
        Optional<Media> existFile = mediaRepository.findByHashAndName(media.getHash(), media.getName());
        return existFile.orElseGet(() -> mediaRepository.save(media));
    }

    private @Nullable Media saveMedia0(URI remote, Response response) throws IOException {
        return saveMedia0(remote, response, null, false);
    }

    @SuppressWarnings("SameParameterValue")
    private @Nullable Media saveMedia0(URI remote, Response response, boolean decompressGzip) throws IOException {
        return saveMedia0(remote, response, null, decompressGzip);
    }

    private @Nullable Media saveMedia0(URI remote, String contentType, Response response) throws IOException {
        return saveMedia0(remote, response, contentType, false);
    }

    private @Nullable Media saveMedia0(URI remote, @NotNull Response response, String contentType, boolean decompressGzip) throws IOException {
        Media media = new Media();
        if (response.isSuccessful()) {
            if (contentType == null) {
                media.setContentType(response.header("Content-Type"));
            } else {
                media.setContentType(contentType);
            }
            // save to local
            assert response.body() != null;
            InputStream inputStream = response.body().byteStream();
            if (decompressGzip) {
                inputStream = new GZIPInputStream(inputStream);
            }
            FileMetadata metadata = saveToLocal(inputStream);
            String parsedFileName = parseFileName(response.header("Content-Disposition"));
            if (parsedFileName != null) {
                media.setName(parsedFileName);
            } else {
                String[] path = remote.getPath().split("/");
                media.setName(path[path.length - 1]);
            }
            if (media.getName().endsWith(".tgs") && decompressGzip && media.getContentType().equals("application/octet-stream")) {
                media.setContentType("application/json"); // lottie animation from Telegram
                String filename = media.getName();
                media.setName(filename.substring(0, filename.length() - 4) + ".json");
            }
            media.setHash(metadata.getHash());
            compress(media.getContentType(), null, media);
        } else {
            log.info("Failed to download file from {} ({})", remote, response.code());
            return null;
        }
        return media;
    }

    @Override
    public void fromRemote(URI remote, String contentType, Consumer<Media> consumer) throws MalformedURLException {
        log.info("Download {} (async)", remote);
        okHttpClient.newCall(new Request.Builder()
            .url(remote.toURL())
            .header("User-Agent", userAgent)
            .build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.info("Failed to download file from {}", remote, e);
                consumer.accept(null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Media media = mediaRepository.save(Objects.requireNonNull(saveMedia0(remote, contentType, response)));
                consumer.accept(media);
            }
        });
    }

    @Override
    public Media findById(String id) {
        return mediaRepository.findById(id).orElse(null);
    }

    @Override
    public StreamMetadata openInputStream(Media media) throws IOException {
        if (media == null) {
            return null;
        }
        File file = getFileByHash(media.getHash());
        if (!file.exists()) {
            return null;
        }
        return StreamMetadata.builder()
            .inputStream(FileUtils.openInputStream(file))
            .size(file.length())
            .build();
    }

    @Override
    public Media upload(InputStream inputStream, String filename, String contentType, User uploader) throws IOException {
        Media media = new Media();
        media.setName(filename);
        log.info("Upload {}", media.getName());
        media.setContentType(contentType);
        media.setUploader(uploader);
        FileMetadata metadata = saveToLocal(inputStream);
        media.setHash(metadata.getHash());
        log.info("Uploaded {}", media.getName());
        compress(contentType, uploader, media);
        return mediaRepository.save(media);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void compress(String contentType, User uploader, Media media) throws IOException {
        MimeType givenContentType = MimeType.valueOf(contentType);
        MimeType imageType = MimeType.valueOf("image/*");
        MimeType webpType = MimeType.valueOf("image/webp");
        if (imageType.isCompatibleWith(givenContentType) && !webpType.isCompatibleWith(givenContentType)) {
            log.info("Generate compressed image for {}", media.getName());
            Media compressed = new Media();
            compressed.setName(media.getName().substring(0, media.getName().lastIndexOf(".")) + "-compressed.webp");
            compressed.setUploader(uploader);
            compressed.setContentType("image/webp");
            // process image
            File tmp = createTempFile();
            compressUtil.compressImage(FileUtils.openInputStream(getFileByHash(media.getHash())), tmp);
            // calc hash
            String hash = SecureUtil.sha256(tmp);
            // move file
            File target = getFileByHash(hash);
            if (target.exists()) {
                tmp.delete();
            } else {
                FileUtils.moveFile(tmp, target);
            }
            compressed.setHash(hash);
            media.setCompressed(mediaRepository.save(compressed));
        }
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
    @Contract("_ -> new")
    private @NotNull File getFileByHash(String hash) {
        return new File(this.getDataFolder(), "files/" + hash);
    }

    @Contract(" -> new")
    private @NotNull File getDataFolder() {
        return new File(FileUtils.current(), ".qbychat");
    }

    @Override
    public boolean hasFile(String sha256) {
        return getFileByHash(sha256).exists();
    }

    @Override
    public @NotNull Optional<Media> fromExist(User user, String hash, String name, String contentType) {
        if (!hasFile(hash)) {
            return Optional.empty();
        }
        Media media = new Media();
        media.setHash(hash);
        media.setName(name);
        media.setUploader(user);
        media.setContentType(contentType);
        return Optional.of(mediaRepository.save(media));
    }

    @Override
    public Media extractGzip(URI remote, @NotNull Response response) throws IOException {
        Media media = saveMedia0(remote, response, true);
        if (media == null) return null;
        Optional<Media> existFile = mediaRepository.findByHashAndName(media.getHash(), media.getName());
        return existFile.orElseGet(() -> mediaRepository.save(media));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private @NotNull File createTempFile() throws IOException {
        File tmp = new File(new File(getDataFolder(), "temp"), UUID.randomUUID().toString());
        FileUtils.createParentDirectories(tmp);
        tmp.createNewFile();
        return tmp;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private @NotNull FileMetadata saveToLocal(InputStream source) throws IOException {
        File tmp = createTempFile();
        TeeInputStream inputStream = new TeeInputStream(source, FileUtils.openOutputStream(tmp), true);
        // calculate sha256
        String sha256 = SecureUtil.sha256(inputStream);
        File file = getFileByHash(sha256);
        // generate metadata
        FileMetadata metadata = new FileMetadata();
        metadata.setHash(sha256);
        metadata.setFile(file);
        if (file.exists()) {
            tmp.delete();
            return metadata;
        }
        // move file
        FileUtils.moveFile(tmp, file);
        return metadata;
    }

    @Data
    public static class FileMetadata {
        private File file;
        private String hash;
    }
}
