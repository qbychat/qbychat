package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.LinkPreview;
import org.qbynet.chat.entity.dto.LinkPreviewDTO;
import org.qbynet.chat.entity.vo.LinkPreviewVO;
import org.qbynet.chat.service.LinkPreviewService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.net.URI;
import java.util.concurrent.ForkJoinPool;

@RestController
@RequestMapping("/api/link")
public class LinkController {
    @Resource
    LinkPreviewService linkPreviewService;

    @PostMapping("preview")
    @Secured("SCOPE_link.preview")
    public DeferredResult<ResponseEntity<RestBean<LinkPreviewVO>>> preview(@RequestBody LinkPreviewDTO dto) {
        DeferredResult<ResponseEntity<RestBean<LinkPreviewVO>>> result = new DeferredResult<>(5000L);
        result.onTimeout(() -> result.setResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(RestBean.failure(408, "Timeout"))));
        ForkJoinPool.commonPool().submit(() -> {
            URI uri = URI.create(dto.getLink());
            LinkPreview linkPreview = linkPreviewService.generateOrGetLinkPreview(uri);
            if (linkPreview == null) {
                result.setResult(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(RestBean.failure(422, "Unprocessable Entity")));
                return;
            }
            result.setResult(ResponseEntity.ok(RestBean.success(LinkPreviewVO.from(linkPreview))));
        });
        return result;
    }
}
