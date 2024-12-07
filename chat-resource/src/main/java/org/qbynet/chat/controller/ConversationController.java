package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.JoinRequest;
import org.qbynet.chat.entity.Member;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.ApproveJoinRequestDTO;
import org.qbynet.chat.entity.dto.CreateConversationDTO;
import org.qbynet.chat.entity.vo.ConversationVO;
import org.qbynet.chat.entity.vo.JoinConversationVO;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    @Resource
    ConversationService conversationService;

    @PostMapping("create")
    public ResponseEntity<RestBean<ConversationVO>> createConversation(@RequestBody CreateConversationDTO dto, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.create(dto.getName(), dto.getType(), user);
        return ResponseEntity.ok(RestBean.success(ConversationVO.from(conversation)));
    }

    @GetMapping("{link}/info")
    public ResponseEntity<RestBean<ConversationVO>> getConversation(@PathVariable("link") String link, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findByLink(link);
        // check is banned
        if (conversation == null || conversationService.isBanned(conversation, user)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Conversation not found"));
        }
        return ResponseEntity.ok(RestBean.success(ConversationVO.from(conversation)));
    }

    @PostMapping("{link}/join")
    public ResponseEntity<RestBean<JoinConversationVO>> joinConversation(@PathVariable String link, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findByLink(link);
        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Conversation not found"));
        }
        ConversationService.JoinConversationDetails details = conversationService.join(conversation, user);
        return ResponseEntity.ok(RestBean.success(JoinConversationVO.builder()
                .joined(details.isJoined())
                .banned(details.isBanned())
                .build()));
    }

    @PostMapping("approve")
    public ResponseEntity<RestBean<String>> approveJoinRequest(@RequestBody ApproveJoinRequestDTO dto, @RequestAttribute("user") User user) {
        JoinRequest joinRequest = conversationService.findJoinRequest(dto.getRequest());
        if (joinRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Cannot find request."));
        }
        // check permission
        Member member = conversationService.findMember(joinRequest.getConversation(), user);
        if (member == null || !conversationService.canApproveJoinRequest(member)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "No permission to approve this request"));
        }
        // do approve
        conversationService.approveJoinRequest(joinRequest);
        return ResponseEntity.ok(RestBean.success("Request approved"));
    }
}
