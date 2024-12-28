package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.repository.ConversationRepository;
import org.qbynet.chat.repository.MemberRepository;
import org.qbynet.chat.service.AuditLogService;
import org.qbynet.chat.service.ConversationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Log4j2
@Service
public class ConversationServiceImpl implements ConversationService {
    @Resource
    ConversationRepository conversationRepository;

    @Resource
    MemberRepository memberRepository;

    @Resource
    AuditLogService auditLogService;

    @Override
    public Mono<Conversation> create(String name, ConversationType type, User owner) {
        Conversation conversation = new Conversation();
        conversation.setName(name);
        conversation.setType(type);
        if (type == ConversationType.CHANNEL) {
            conversation.setDefaultPermissions(Collections.singletonList(MemberPermission.CHANNEL_DEFAULT));
        } else if (type == ConversationType.PRIVATE_CHAT) {
            conversation.setDefaultPermissions(Collections.singletonList(MemberPermission.PRIVATE_CHAT_DEFAULT));
        }
        return conversationRepository.save(conversation)
            .flatMap((savedConversation) -> {
                // add member to conversation
                Member member = new Member();
                member.setUser(owner);
                member.setConversation(savedConversation);
                member.setOwner(true);
                log.info("Conversation {} was created", conversation.getName());
                return memberRepository.save(member)
                    .then(auditLogService.createConversation(savedConversation, owner))
                    .thenReturn(savedConversation);
            });
    }

    @Override
    public Mono<Member> findMember(Conversation conversation, User user) {
        return memberRepository.findByConversationAndUser(conversation, user);
    }

    @Override
    public Mono<Conversation> findConversation(String id) {
        return conversationRepository.findById(id);
    }
}
