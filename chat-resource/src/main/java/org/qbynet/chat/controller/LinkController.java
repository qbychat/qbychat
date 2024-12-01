package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.LinkPreview;
import org.qbynet.chat.entity.dto.LinkPreviewDTO;
import org.qbynet.chat.entity.vo.LinkPreviewVO;
import org.qbynet.chat.service.LinkPreviewService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/link")
public class LinkController {
    @Resource
    LinkPreviewService linkPreviewService;

    @PostMapping("preview")
    public ResponseEntity<RestBean<LinkPreviewVO>> preview(@RequestBody LinkPreviewDTO dto) {
        URI uri = URI.create(dto.getLink());
        LinkPreview linkPreview = linkPreviewService.generateOrGetLinkPreview(uri);
        if (linkPreview == null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(RestBean.failure(422, "Unprocessable Entity"));
        }
        return ResponseEntity.ok(RestBean.success(LinkPreviewVO.from(linkPreview)));
    }
}
