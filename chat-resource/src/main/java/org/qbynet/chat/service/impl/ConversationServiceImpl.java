package org.qbynet.chat.service.impl;

import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.repository.*;
import org.qbynet.chat.service.ConversationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
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
    AvatarRepository avatarRepository;

    @Value("${qbychat.conversation.invite.expire}")
    int inviteExpire;

    @Override
    public Conversation create(String name, ConversationType type, User user) {
        Conversation conversation = new Conversation();
        conversation.setName(name);
        conversation.setType(type);
        if (type == ConversationType.CHANNEL) {
            conversation.setDefaultPermissions(Collections.singletonList(MemberPermission.CHANNEL_DEFAULT));
        } else if (type == ConversationType.PRIVATE_MESSAGE) {
            conversation.setDefaultPermissions(Collections.singletonList(MemberPermission.PRIVATE_CHAT_DEFAULT));
        }
        // add member to conversation
        Conversation savedConversation = conversationRepository.save(conversation);

        Member member = new Member();
        member.setUser(user);
        member.setConversation(savedConversation);
        member.setOwner(true);
        memberRepository.save(member);
        log.info("Conversation {} was created", conversation.getName());
        return savedConversation;
    }

    @Override
    public Member addMember(@NotNull Conversation conversation, User user) {
        Member member = new Member();
        member.setUser(user);
        member.setConversation(conversation);
        log.info("Add user {} to conversation {}", user.getNickname(), conversation.getName());
        return memberRepository.save(member);
    }

    @Override
    public void removeMember(@NotNull Member member) {
        log.info("User {} was removed from conversation {}", member.getUser().getNickname(), member.getConversation().getName());
        member.setNickname(null); // restore nickname
        member.setQuit(true);
        memberRepository.save(member);
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
            return inviteLinkRepository.findByLinkAndExpireAtAfter(link, Instant.now()).map(it -> it.getCreateBy().getConversation()).orElse(null);
        });
    }

    @Override
    public JoinConversationDetails join(@NotNull Conversation conversation, User user) {
        // find the exist member
        Optional<Member> existMemberOptional = memberRepository.findByUserAndConversation(user, conversation);
        if (existMemberOptional.isPresent()) {
            Member existMember = existMemberOptional.get();
            if (existMember.getBanUntil().isAfter(Instant.now())) {
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
        if (!conversation.isMemberVerificationNeeded()) {
            log.info("User {} has joined the conversation {}", user.getNickname(), conversation.getName());
            this.addMember(conversation, user);
            return JoinConversationDetails.builder()
                    .joined(true)
                    .banned(false)
                    .conversation(conversation)
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
    public boolean hasAllPermissions(@NotNull Member member, MemberPermission... permissions) {
        return new HashSet<>(member.getPermissions()).containsAll(List.of(permissions));
    }

    @Override
    public boolean canApproveJoinRequest(@NotNull Member member) {
        return member.getPermissions().contains(MemberPermission.APPROVE_JOIN_REQUESTS);
    }

    @Override
    public void approveJoinRequest(@NotNull JoinRequest request) {
        addMember(request.getConversation(), request.getUser());
        joinRequestRepository.delete(request);
    }

    @Override
    public boolean isBanned(Conversation conversation, User user) {
        return Boolean.TRUE.equals(memberRepository.findByUserAndConversation(user, conversation).map(it -> it.getBanUntil() != null && it.getBanUntil().isAfter(Instant.now())).orElse(null));
    }

    @Override
    public Conversation findConversationById(String conversation) {
        return conversationRepository.findById(conversation).orElse(null);
    }

    @Override
    public Member findMember(Conversation conversation, User user) {
        return memberRepository.findByUserAndConversation(user, conversation).orElse(null);
    }

    @Override
    public JoinRequest findJoinRequest(String id) {
        return joinRequestRepository.findById(id).orElse(null);
    }

    @Override
    public List<Conversation> list(User user) {
        return memberRepository.findAllByUser(user).stream().map(Member::getConversation).toList();
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
            return null;
        }
        inviteLink.setCreateBy(member.get());
        inviteLink.setLink("+" + RandomUtil.randomString(16));
        inviteLink.setExpireAt(Instant.now().plus(inviteExpire, ChronoUnit.DAYS));
        return inviteLinkRepository.save(inviteLink);
    }

    @Override
    public boolean hasJoined(Conversation conversation, User user) {
        return memberRepository.existsByUserAndConversation(user, conversation);
    }

    @Override
    public void switchAutoDeleteTimer(@NotNull Conversation conversation, int duration) {
        conversation.setAutoDeleteTimer(duration);
        // apply for exist messages
        messageRepository.saveAll(messageRepository.findAllByExpiresAtNullOrExpiresAtGreaterThan(Instant.now()).stream().peek(it -> {
            if (duration == -1) {
                it.setExpiresAt(null); // disable timer
            } else {
                it.setExpiresAt(Instant.now().plus(duration, ChronoUnit.DAYS));
            }
        }).toList());
    }

    @Override
    public Avatar findLatestAvatar(Conversation conversation) {
        return avatarRepository.findFirstByConversation(conversation).orElse(null);
    }

    @Override
    public List<Avatar> findAllAvatars(Conversation conversation) {
        return avatarRepository.findAllByConversation(conversation);
    }
}
