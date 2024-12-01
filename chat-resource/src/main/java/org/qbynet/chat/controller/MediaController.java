package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.vo.MediaVO;
import org.qbynet.chat.service.MediaService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    @Resource
    MediaService mediaService;

    /**
     * Get the info of a media
     *
     * @param hash media's sha256
     */
    @GetMapping("{hash}/info")
    public ResponseEntity<RestBean<MediaVO>> mediaInfo(@PathVariable String hash) {
        Media media = mediaService.findByHash(hash);
        if (media == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Media Not Found"));
        }
        return ResponseEntity.ok(RestBean.success(MediaVO.from(media)));
    }

    /**
     * Download a media
     *
     * @param hash sha256 hash
     */
    @GetMapping("{hash}/download")
    public void mediaDownload(@PathVariable String hash, HttpServletResponse response) throws Exception {
        Media media = mediaService.findByHash(hash);
        InputStream inputStream = mediaService.openInputStream(media);
        if (inputStream == null) {
            // file not found
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(RestBean.failure(404, "Not Found").toJson());
            return;
        }
        response.addHeader("Content-Disposition", "attachment; filename=\"" + media.getName() + "\"");
        response.setContentType(media.getContentType());
        IOUtils.copy(inputStream, response.getOutputStream());
    }

    @PutMapping("upload")
    public ResponseEntity<RestBean<List<MediaVO>>> upload(HttpServletRequest request, @RequestAttribute("user") User user) throws Exception {
        JakartaServletFileUpload upload = new JakartaServletFileUpload();
        FileItemInputIterator iterStream = upload.getItemIterator(request);
        List<MediaVO> vos = new ArrayList<>();
        while (iterStream.hasNext()) {
            FileItemInput item = iterStream.next();
            InputStream stream = item.getInputStream();
            if (!item.isFormField()) {
                mediaService.upload(stream, item.getName(), item.getContentType(), user);
            }
        }
        return ResponseEntity.ok(RestBean.success(vos));
    }
}
