package org.qbynet.chat.service.impl;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.i18n.LdLocale;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.repository.*;
import org.qbynet.chat.service.LinkPreviewService;
import org.qbynet.chat.service.MessageService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    MessageRepository messageRepository;

    @Resource
    MediaRepository mediaRepository;

    @Resource
    ReadRepository readRepository;

    @Resource
    MemberRepository memberRepository;

    @Resource
    ConversationRepository conversationRepository;

    @Resource
    StickerRepository stickerRepository;

    @Resource
    LinkPreviewService linkPreviewService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource(name = "messagesQueue")
    Queue messagesQueue;

    @Resource
    LanguageDetector languageDetector;

    @Override
    public Message send(Message source) {
        Message message = messageRepository.save(source);
        rabbitTemplate.convertAndSend(messagesQueue.getName(), message);
        return message;
    }

    @Override
    public Message send(@NotNull SendMessageDTO dto, User user) {
        Conversation conversation = conversationRepository.findById(dto.getConversation()).orElseThrow();
        Member member = memberRepository.findByUserAndConversation(user, conversation).orElseThrow();

        Message message = new Message();
        message.setConversation(conversation);
        // add content
        if (dto.getContent() != null) {
            message.setContent(dto.getContent());
        } else if (dto.getSticker() != null) {
            message.setSticker(stickerRepository.findById(dto.getSticker()).orElseThrow());
        } else {
            throw new IllegalStateException("Please provide a content");
        }
        // add medias
        message.setMedias(dto.getMedias().stream().map(it -> mediaRepository.findById(it).orElse(null)).filter(Objects::nonNull).toList());
        // set type
        message.setType(MessageType.NORMAL_MESSAGE);
        // detect language
        Optional<LdLocale> lang = languageDetector.detect(message.getContent());
        if (lang.isPresent()) {
            message.setLanguage(lang.get().getLanguage());
        }

        // enable auto delete timer
        if (conversation.getAutoDeleteTimer() != -1) {
            message.setExpiresAt(Instant.now().plus(conversation.getAutoDeleteTimer(), ChronoUnit.DAYS));
        }
        // sender info
        message.setSender(member);
        message.setAnonymous(member.isAnonymous() && dto.isAnonymous());
        if (dto.getRedirectFrom() != null) {
            message.setRedirect(messageRepository.findById(dto.getRedirectFrom()).orElseThrow());
        }
        if (dto.getReplyTo() != null) {
            message.setReply(messageRepository.findById(dto.getReplyTo()).orElseThrow());
        }
        // generate link preview
        try {
            if (dto.isLinkPreview()) {
                message.setLinkPreview(linkPreviewService.fromText(message.getContent()));
            }
        } catch (Exception ignored) {
            // ignored
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
    public void markAsRead(@NotNull List<String> messageIds, User user) {
        List<Message> messages = messageIds.stream().map(messageId -> messageRepository.findById(messageId).orElse(null)).filter(Objects::nonNull).toList();
        readRepository.saveAll(messages.stream().map(it -> {
            Member member = memberRepository.findByUserAndConversation(user, it.getConversation()).orElse(null);
            if (member == null) {
                return null;
            }
            return Read.builder().message(it).member(member).build();
        }).filter(Objects::nonNull).toList());
    }

    @Scheduled(cron = "0 0 */6 * * *")
    private void autoDeleteExpiredMessages() {
        log.debug("Delete expired messages");
        messageRepository.deleteAllByExpiresAtLessThan(Instant.now());
    }

    @Override
    public Message findMessageById(String id) {
        return messageRepository.findById(id).orElse(null);
    }

    @Override
    public void editMessage(@NotNull Message message, String content, String sticker, List<String> medias, boolean linkPreview) {
        message.setEditAt(Instant.now());
        message.setContent(content);
        message.setMedias(mediaRepository.findAllById(medias));
        message.setSticker(stickerRepository.findById(sticker).orElse(null));
        if (linkPreview) {
            message.setLinkPreview(linkPreviewService.fromText(message.getContent()));
        } else {
            message.setLinkPreview(null);
        }
        Optional<LdLocale> lang = languageDetector.detect(message.getContent());
        if (lang.isPresent()) {
            message.setLanguage(lang.get().getLanguage());
        }
        rabbitTemplate.convertAndSend(messagesQueue.getName(), messageRepository.save(message));
    }


    @Override
    public Page<Message> fetchMessages(Conversation conversation, User user, Pageable pageable) {
        Member member = memberRepository.findByUserAndConversation(user, conversation).orElse(null);
        if (member == null) return null;
        Page<Message> messages = messageRepository.findAllByConversationOrderBySentAtDesc(conversation, pageable);
        if (messages.getContent().isEmpty()) return null;
        return messages;
    }
}
