package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.ApproveJoinRequestDTO;
import org.qbynet.chat.entity.dto.ConfigAutoDeleteTimerDTO;
import org.qbynet.chat.entity.dto.CreateConversationDTO;
import org.qbynet.chat.entity.dto.InviteDTO;
import org.qbynet.chat.entity.vo.*;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    @Resource
    ConversationService conversationService;

    @GetMapping("session")
    public ResponseEntity<RestBean<ConversationSession>> session(@RequestParam(name = "id") String id, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findConversationById(id);
        Member member = conversationService.findMember(conversation, user);
        if (member == null) {
            JoinRequest joinRequest = conversationService.findJoinRequest(conversation, user);
            return ResponseEntity.ok(RestBean.success(ConversationSession.builder()
                .conversation(ConversationVO.from(conversation))
                .member(null)
                .joined(false)
                .joinRequest(JoinRequestVO.from(joinRequest))
                .build()));
        }
        int joinRequests = -1;
        if (member.hasPermissions(MemberPermission.PROCESS_JOIN_REQUESTS)) {
            joinRequests = conversationService.countJoinRequests(conversation);
        }
        return ResponseEntity.ok(RestBean.success(ConversationSession.builder()
            .conversation(ConversationVO.from(conversation))
            .member(MemberVO.from(member))
            .joined(true)
            .joinRequests(joinRequests)
            .build()));
    }

    @GetMapping("list")
    public ResponseEntity<RestBean<List<ConversationUserVO>>> list(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(RestBean.success(conversationService.list(user).stream().map((conversation -> {
            Member member = conversationService.findMember(conversation, user);
            return ConversationUserVO.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .type(conversation.getType())
                .notificationPreferment(member.getNotifications())
                .pinned(member.isPinned())
                .build();
        })).toList()));
    }

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

    @PostMapping("invite")
    public ResponseEntity<RestBean<InviteLinkVO>> invite(@RequestBody InviteDTO dto, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findConversationById(dto.getConversation());
        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Conversation not found"));
        }
        InviteLink inviteLink = conversationService.invite(conversation, user);
        if (inviteLink == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "You have no permission to invite"));
        }
        return ResponseEntity.ok(RestBean.success(InviteLinkVO.from(inviteLink)));
    }

    @GetMapping("join-request")
    public ResponseEntity<RestBean<List<JoinRequestVO>>> listJoinRequests(@RequestParam(name = "conversation") String conversationId, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findConversationById(conversationId);
        if (!conversationService.findMember(conversation, user).hasPermissions(MemberPermission.PROCESS_JOIN_REQUESTS)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "You have no permission to view join requests"));
        }
        List<JoinRequest> requests = conversationService.findAllJoinRequests(conversation);
        return ResponseEntity.ok(RestBean.success(requests.stream().map(JoinRequestVO::from).toList()));
    }

    @PostMapping("join-request")
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
        conversationService.approveJoinRequest(joinRequest, user);
        return ResponseEntity.ok(RestBean.success("Request approved"));
    }

    @PostMapping("auto-delete-timer")
    public ResponseEntity<RestBean<?>> configAutoDeleteTimer(@RequestBody ConfigAutoDeleteTimerDTO dto, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findConversationById(dto.getConversation());
        Member member = conversationService.findMember(conversation, user);
        if (member == null || !member.hasPermissions(MemberPermission.MANAGE_AUTO_DELETE_TIMER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "Forbidden"));
        }
        conversationService.switchAutoDeleteTimer(conversation, dto.getDuration(), user);
        return ResponseEntity.ok(RestBean.success("Ok"));
    }
}
