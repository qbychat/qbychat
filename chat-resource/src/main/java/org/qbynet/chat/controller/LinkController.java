package org.qbynet.chat.controller;

import org.qbynet.chat.entity.dto.LinkPreviewDTO;
import org.qbynet.chat.entity.vo.LinkPreviewVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/link")
public class LinkController {
    @PostMapping("preview")
    public LinkPreviewVO preview(@RequestBody LinkPreviewDTO dto) {
        // todo
        return null;
    }
}
