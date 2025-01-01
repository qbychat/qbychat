package org.qbynet.chat.controller.graphql;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.annotation.Authorized;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.ConfigAutoDeleteTimerDTO;
import org.qbynet.chat.entity.dto.CreateConversationDTO;
import org.qbynet.chat.entity.dto.InviteDTO;
import org.qbynet.chat.entity.vo.*;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.exception.BadRequest;
import org.qbynet.shared.exception.Forbidden;
import org.qbynet.shared.exception.NotFound;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ConversationController {
    @Resource
    UserService userService;

    @Resource
    ConversationService conversationService;

    @MutationMapping
    @Secured("SCOPE_conversation.create")
    public ConversationVO createConversation(@Argument @NotNull CreateConversationDTO input) {
        if (input.getType().equals(ConversationType.PRIVATE_CHAT)) {
            throw new BadRequest("You cannot create a private chat via this API");
        }
        User user = userService.currentUser();
        Conversation conversation = conversationService.create(input.getName(), input.getType(), user);
        return ConversationVO.from(conversation);
    }

    @Authorized
    @QueryMapping
    public ConversationVO conversationByLink(@Argument @NotNull String link) {
        User user = userService.currentUser();
        Conversation conversation = conversationService.findByLink(link);
        if (conversation == null || conversationService.isBanned(conversation, user)) {
            throw new NotFound("Conversation not found");
        }
        return ConversationVO.from(conversation);
    }

    @MutationMapping
    @Secured("SCOPE_conversation.join")
    public JoinConversationVO joinConversation(@Argument @NotNull String link) {
        Conversation conversation = conversationService.findByLink(link);
        User user = userService.currentUser();
        if (conversation == null || conversationService.isBanned(conversation, user)) {
            throw new NotFound("Conversation not found");
        }
        ConversationService.JoinConversationDetails details = conversationService.join(conversation, user);
        return JoinConversationVO.builder()
            .joined(details.isJoined())
            .banned(details.isBanned())
            .build();
    }

    @QueryMapping
    @Secured("SCOPE_conversation.list")
    public List<ConversationMemberVO> joinedConversations() {
        User user = userService.currentUser();
        return conversationService.listJoinedConversations(user).stream().map((ConversationMemberVO::from)).toList();
    }

    @MutationMapping
    @Secured("SCOPE_conversation.invite")
    public InviteLink invite(@Argument @NotNull InviteDTO input) {
        return conversationService.invite(input);
    }

    @QueryMapping
    @Secured("SCOPE_conversation.join-request.list")
    public List<JoinRequestVO> joinRequests(@Argument @NotNull String conversation) {
        User user = userService.currentUser();
        Conversation conversation1 = conversationService.findConversationById(conversation);
        if (!conversationService.findMember(conversation1, user).hasPermissions(MemberPermission.PROCESS_JOIN_REQUESTS)) {
            throw new Forbidden("You have no permission to view join requests");
        }
        List<JoinRequest> requests = conversationService.findAllJoinRequests(conversation1);
        return requests.stream().map(JoinRequestVO::from).toList();
    }

    @MutationMapping
    @Secured("SCOPE_conversation.join-request.process")
    public MemberVO approveJoinRequest(@Argument String id) {
        //noinspection DuplicatedCode
        User user = userService.currentUser();
        JoinRequest joinRequest = conversationService.findJoinRequest(id);
        if (joinRequest == null) {
            throw new NotFound("Join request not found");
        }
        // check permission
        Member member = conversationService.findMember(joinRequest.getConversation(), user);
        if (member == null || !conversationService.canApproveJoinRequest(member)) {
            throw new Forbidden("You have no permission to approve join request");
        }
        // do approve
        return MemberVO.from(conversationService.approveJoinRequest(joinRequest));
    }

    @MutationMapping
    @Secured("SCOPE_conversation.join-request.process")
    public String denyJoinRequest(@Argument @NotNull String id) {
        //noinspection DuplicatedCode
        User user = userService.currentUser();
        JoinRequest joinRequest = conversationService.findJoinRequest(id);
        if (joinRequest == null) {
            throw new NotFound("Join request not found");
        }
        // check permission
        Member member = conversationService.findMember(joinRequest.getConversation(), user);
        if (member == null || !conversationService.canApproveJoinRequest(member)) {
            throw new Forbidden("You have no permission to approve join request");
        }
        // deny
        conversationService.denyJoinRequest(joinRequest);
        return "Success";
    }

    @MutationMapping
    @Secured("SCOPE_conversation.manage")
    public ConversationVO configAutoDeleteTimer(@Argument @NotNull ConfigAutoDeleteTimerDTO input) {
        User user = userService.currentUser();
        Conversation conversation = conversationService.findConversationById(input.getConversation());
        Member member = conversationService.findMember(conversation, user);
        if (member == null || !member.hasPermissions(MemberPermission.MANAGE_AUTO_DELETE_TIMER)) {
            throw new Forbidden("You have no permission to manage auto delete timer");
        }
        return ConversationVO.from(conversationService.switchAutoDeleteTimer(conversation, input.getDuration(), user));
    }

    @Authorized
    @QueryMapping
    public ConversationSession conversationSession(@Argument String id) {
        User user = userService.currentUser();
        Conversation conversation = conversationService.findConversationById(id);
        Member member = conversationService.findMember(conversation, user);
        if (conversation == null || conversationService.isBanned(conversation, user)) {
            throw new NotFound("Conversation not found");
        }
        if (member == null) {
            JoinRequest joinRequest = conversationService.findJoinRequest(conversation, user);
            return ConversationSession.builder()
                .conversation(ConversationVO.from(conversation))
                .member(null)
                .joined(false)
                .joinRequestPending(joinRequest != null)
                .build();
        }
        int joinRequests = -1;
        if (member.hasPermissions(MemberPermission.PROCESS_JOIN_REQUESTS)) {
            joinRequests = conversationService.countJoinRequests(conversation);
        }
        return ConversationSession.builder()
            .conversation(ConversationVO.from(conversation))
            .member(MemberVO.from(member))
            .joined(true)
            .joinRequests(joinRequests)
            .build();
    }
}
