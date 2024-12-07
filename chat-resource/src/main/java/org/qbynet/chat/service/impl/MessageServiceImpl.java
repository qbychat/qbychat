package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.repository.ConversationRepository;
import org.qbynet.chat.repository.MediaRepository;
import org.qbynet.chat.repository.MemberRepository;
import org.qbynet.chat.repository.MessageRepository;
import org.qbynet.chat.service.LinkPreviewService;
import org.qbynet.chat.service.MessageService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Log4j2
@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    MessageRepository messageRepository;

    @Resource
    MediaRepository mediaRepository;

    @Resource
    MemberRepository memberRepository;

    @Resource
    ConversationRepository conversationRepository;

    @Resource
    LinkPreviewService linkPreviewService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource(name = "messagesQueue")
    Queue messagesQueue;

    @Override
    public Message send(Message source) {
        Message message = messageRepository.save(source);
        rabbitTemplate.convertAndSend(messagesQueue.getName(), message);
        return message;
    }

    @Override
    public Message send(SendMessageDTO dto, User user) {
        Conversation conversation = conversationRepository.findById(dto.getConversation()).orElseThrow();
        Member member = memberRepository.findByUserAndConversation(user, conversation).orElseThrow();

        Message message = new Message();
        message.setContent(dto.getContent());
        message.setMedias(dto.getMedias().stream().map(it -> mediaRepository.findById(it).orElse(null)).filter(Objects::nonNull).toList());
        // sender info
        if (!member.isAnonymous()) {
            message.setSender(member);
        }
        if (dto.getRedirectFrom() != null) {
            message.setRedirect(messageRepository.findById(dto.getRedirectFrom()).orElseThrow());
        }
        if (dto.getReplyTo() != null) {
            message.setReply(messageRepository.findById(dto.getReplyTo()).orElseThrow());
        }
        // generate link preview
        if (dto.isLinkPreview()) {
            message.setLinkPreview(linkPreviewService.fromText(message.getContent()));
        }
        return send(message);
    }

    @Override
    public boolean canSendMessage(Conversation conversation, User user) {
        Member member = memberRepository.findByUserAndConversation(user, conversation).orElse(null);
        if (member == null) {
            return false; // this user haven't joined the conversation
        }
        if (member.isOwner()) {
            return true;
        }
        // muted or banned
        if (member.getMuteUntil().isAfter(Instant.now()) || member.getBanUntil().isAfter(Instant.now())) {
            return false;
        }
        return member.getPermissions() != null && member.getPermissions().contains(MemberPermission.SEND_TEXT_MESSAGE);
    }

    @Override
    public Conversation findConversationById(String id) {
        return conversationRepository.findById(id).orElse(null);
    }

    @RabbitListener
    private void onMessage(Message message) {
        log.info("Message received: {}", message.getId());
    }

    @Scheduled(cron = "0 0 */6 * * *")
    private void autoDeleteExpiredMessages() {
        log.info("Delete expired messages");
        messageRepository.deleteByExpiresAtLessThan(Instant.now());
    }
}
