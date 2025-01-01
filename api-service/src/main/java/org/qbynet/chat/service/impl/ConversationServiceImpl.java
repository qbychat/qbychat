package org.qbynet.chat.service.impl;

import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.InviteDTO;
import org.qbynet.chat.repository.*;
import org.qbynet.chat.service.AuditLogService;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.EventService;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.exception.Forbidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class ConversationServiceImpl implements ConversationService {

    @Resource
    ConversationRepository conversationRepository;

    @Resource
    MemberRepository memberRepository;

    @Resource
    JoinRequestRepository joinRequestRepository;

    @Resource
    InviteLinkRepository inviteLinkRepository;

    @Resource
    MessageRepository messageRepository;

    @Resource
    UserService userService;

    @Resource
    AuditLogService auditLogService;

    @Resource
    EventService eventService;

    @Value("${qbychat.conversation.invite.expire}")
    int inviteExpire;

    @Override
    public Conversation create(String name, ConversationType type, User owner) {
        Conversation conversation = new Conversation();
        conversation.setName(name);
        conversation.setType(type);
        if (type == ConversationType.CHANNEL) {
            conversation.setDefaultPermissions(Collections.singletonList(MemberPermission.CHANNEL_DEFAULT));
        } else if (type == ConversationType.PRIVATE_CHAT) {
            conversation.setDefaultPermissions(Collections.singletonList(MemberPermission.PRIVATE_CHAT_DEFAULT));
        }
        // add member to conversation
        Conversation savedConversation = conversationRepository.save(conversation);

        Member member = new Member();
        member.setUser(owner);
        member.setConversation(savedConversation);
        member.setOwner(true);
        memberRepository.save(member);
        log.info("Conversation {} was created", conversation.getName());
        auditLogService.createConversation(savedConversation, owner);
        return savedConversation;
    }

    @Override
    public Member addMember(@NotNull Conversation conversation, User user) {
        Member member = new Member();
        member.setUser(user);
        member.setConversation(conversation);
        log.info("Add user {} to conversation {}", user.getNickname(), conversation.getName());
        Member saved = memberRepository.save(member);
        auditLogService.memberJoined(saved);
        // push event
        eventService.createEvent(member.getUser(), EventType.JOIN_CONVERSATION, member.getConversation().getId());
        return saved;
    }

    @Override
    public void removeMember(@NotNull Member member) {
        log.info("User {} was removed from conversation {}", member.getUser().getNickname(), member.getConversation().getName());
        member.setNickname(null); // restore nickname
        member.setQuit(true);
        auditLogService.memberQuit(memberRepository.save(member));
        // push event
        eventService.createEvent(member.getUser(), EventType.QUIT_CONVERSATION, member.getConversation().getId());
    }

    @Override
    public void setAnonymous(boolean state, @NotNull Member member) {
        member.setAnonymous(state);
        memberRepository.save(member);
    }

    @Override
    public Conversation findByLink(String link) {
        return conversationRepository.findByLink(link).orElseGet(() -> {
            if (!link.startsWith("+")) {
                // doesn't match any
                return null;
            }
            // find by invite links
            return inviteLinkRepository.findByLinkAndExpireAtAfter(link, Instant.now()).map(it -> it.getOwner().getConversation()).orElse(null);
        });
    }

    @Override
    public JoinConversationDetails join(@NotNull Conversation conversation, User user) {
        // find the exist member
        Optional<Member> existMemberOptional = memberRepository.findByUserAndConversation(user, conversation);
        if (existMemberOptional.isPresent()) {
            Member existMember = existMemberOptional.get();
            if (existMember.getBanUntil() != null && existMember.getBanUntil().isAfter(Instant.now())) {
                // banned
                return JoinConversationDetails.builder()
                    .joined(false)
                    .banned(true)
                    .conversation(existMember.getConversation())
                    .build();
            }
            // already joined
            return JoinConversationDetails.builder()
                .joined(true)
                .banned(false)
                .conversation(existMember.getConversation())
                .build();
        }
        if (!conversation.isVerifyNeeded()) {
            log.info("User {} has joined the conversation {}", user.getNickname(), conversation.getName());
            this.addMember(conversation, user);
            return JoinConversationDetails.builder()
                .joined(true)
                .banned(false)
                .conversation(conversation)
                .build();
        }
        Optional<JoinRequest> exist = joinRequestRepository.findByConversationAndUser(conversation, user);
        if (exist.isPresent()) {
            // already requested
            return JoinConversationDetails.builder()
                .joined(false)
                .banned(false)
                .conversation(conversation)
                .joinRequest(exist.get())
                .build();
        }
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setConversation(conversation);
        joinRequest.setUser(user);
        return JoinConversationDetails.builder()
            .joined(false)
            .banned(false)
            .conversation(conversation)
            .joinRequest(joinRequestRepository.save(joinRequest))
            .build();
    }

    @Override
    public boolean canApproveJoinRequest(@NotNull Member member) {
        return member.getPermissions().contains(MemberPermission.PROCESS_JOIN_REQUESTS);
    }

    @Override
    @Secured("SCOPE_conversation.join-request.manage")
    public Member approveJoinRequest(@NotNull JoinRequest request) {
        User operator = userService.currentUser();

        Member member = addMember(request.getConversation(), request.getUser());
        auditLogService.approveJoinRequest(request, operator);
        joinRequestRepository.delete(request);
        log.info("Join request {} was approved", request.getId());
        return member;
    }

    @Override
    @Secured("SCOPE_conversation.join-request.manage")
    public void denyJoinRequest(JoinRequest joinRequest) {
        User operator = userService.currentUser();
        auditLogService.denyJoinRequest(joinRequest, operator);
        joinRequestRepository.delete(joinRequest);
        log.info("Join request {} was denied", joinRequest.getId());
    }

    @Override
    public boolean hasViewPermission(Conversation conversation, User user) {
        if (hasJoined(conversation, user)) return true;
        return conversation.isPreview();
    }

    @Override
    public Member createPrivateChat(@NotNull User user, @NotNull User partner) {
        // find exist
        Optional<Member> exist = this.listJoinedConversations(partner)
            .stream()
            .filter(it -> it.getConversation().getType().equals(ConversationType.PRIVATE_CHAT))
            .map(member -> this.getPrivateChatPartner(member.getConversation(), member.getUser()))
            .filter(member -> member.getUser().equals(user))
            .findFirst();
        if (exist.isPresent()) {
            return exist.get();
        }
        Conversation conversation = new Conversation();
        conversation.setPreview(false);
        conversation.setNoForward(false);
        conversation.setDefaultPermissions(Collections.singletonList(MemberPermission.PRIVATE_CHAT_DEFAULT));
        conversation.setType(ConversationType.PRIVATE_CHAT);
        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("Created private chat between {} and {}", user.getId(), partner.getId());
        // add members
        Member member = addMember(savedConversation, user);
        addMember(savedConversation, partner);
        return member;
    }

    @Override
    public boolean isBanned(Conversation conversation, User user) {
        Optional<Member> member = memberRepository.findByUserAndConversation(user, conversation);
        if (member.isEmpty()) return false; // not banned
        return Boolean.TRUE.equals(member.map(it -> it.getBanUntil() != null && it.getBanUntil().isAfter(Instant.now())).orElse(false));
    }

    @Override
    public Conversation findConversationById(String conversation) {
        return conversationRepository.findById(conversation).orElse(null);
    }

    @Override
    public Member findMember(Conversation conversation, User user) {
        if (conversation == null || user == null) return null;
        Member member = memberRepository.findByUserAndConversation(user, conversation).orElse(null);
        if (member == null || member.isQuitOrBanned()) return null;
        return member;
    }

    @Override
    public JoinRequest findJoinRequest(String id) {
        return joinRequestRepository.findById(id).orElse(null);
    }

    @Override
    public List<Member> listJoinedConversations(User user) {
        return memberRepository.findAllByUser(user);
    }

    @Override
    public List<Member> listMembers(Conversation conversation) {
        return memberRepository.findAllByConversationAndBanUntilNullOrBanUntilGreaterThan(conversation, Instant.now());
    }

    @Override
    public InviteLink invite(Conversation conversation, User user) {
        InviteLink inviteLink = new InviteLink();
        Optional<Member> member = memberRepository.findByUserAndConversation(user, conversation);
        if (member.isEmpty() || !member.get().getPermissions().contains(MemberPermission.CREATE_INVITE_LINKS)) {
            throw new Forbidden("No permissions to create invite link in this conversation");
        }
        inviteLink.setOwner(member.get());
        inviteLink.setLink("+" + RandomUtil.randomString(16));
        inviteLink.setExpireAt(Instant.now().plus(inviteExpire, ChronoUnit.DAYS));
        return inviteLinkRepository.save(inviteLink);
    }

    @Override
    public boolean hasJoined(Conversation conversation, User user) {
        Optional<Member> optional = memberRepository.findByUserAndConversation(user, conversation);
        if (optional.isEmpty()) return false;
        Member member = optional.get();
        return !member.isQuit() || !member.isBanned();
    }

    @Override
    public @NotNull Conversation switchAutoDeleteTimer(@NotNull Conversation conversation, int duration, User operator) {
        conversation.setAutoDeleteTimer(duration);
        // apply for exist messages
        if (duration == -1) {
            log.info("Disabled auto delete timer for {}", conversation.getName());
        } else {
            log.info("Set auto delete timer for {} (duration={}d)", conversation.getName(), duration);
        }
        messageRepository.saveAll(messageRepository.findAllByExpiresAtNullOrExpiresAtGreaterThan(Instant.now()).stream().peek(it -> {
            if (duration == -1) {
                it.setExpiresAt(null); // disable timer
            } else {
                it.setExpiresAt(Instant.now().plus(duration, ChronoUnit.DAYS));
            }
        }).toList());
        auditLogService.configAutoDeleteTimer(conversation, operator);
        return conversationRepository.save(conversation);
    }

    @Override
    public List<Member> listMembersWithPermissions(Conversation conversation, MemberPermission... permissions) {
        return memberRepository.findAllByConversationAndPermissionsNotNullOrConversationAndOwnerIsTrue(conversation, conversation).stream().filter(it -> it.hasPermissions(permissions)).toList();
    }

    @Override
    public List<JoinRequest> findAllJoinRequests(Conversation conversation) {
        return joinRequestRepository.findAllByConversation(conversation);
    }

    @Override
    public JoinRequest findJoinRequest(Conversation conversation, User user) {
        return joinRequestRepository.findByConversationAndUser(conversation, user).orElse(null);
    }

    @Override
    public int countJoinRequests(Conversation conversation) {
        return joinRequestRepository.countByConversation(conversation);
    }

    @Override
    public Member getPrivateChatPartner(@NotNull Conversation conversation, User self) {
        if (!conversation.getType().equals(ConversationType.PRIVATE_CHAT)) {
            throw new IllegalArgumentException("Only private chat conversations are allowed");
        }
        return memberRepository.findAllByConversation(conversation).stream().filter(it -> !it.getUser().equals(self)).findFirst().orElseThrow(() -> new IllegalStateException("Private chat member not found"));
    }

    @Override
    public InviteLink invite(@NotNull InviteDTO input) {
        return this.invite(conversationRepository.findById(input.getConversation()).orElseThrow(() -> new IllegalArgumentException("Conversation " + input.getConversation() + " not found")), userService.currentUser());
    }
}
