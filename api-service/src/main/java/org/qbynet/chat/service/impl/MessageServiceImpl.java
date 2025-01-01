package org.qbynet.chat.service.impl;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.i18n.LdLocale;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.DeleteMessageDTO;
import org.qbynet.chat.entity.dto.EditMessageDTO;
import org.qbynet.chat.entity.dto.SendMessageDTO;
import org.qbynet.chat.entity.vo.MessageVO;
import org.qbynet.chat.repository.*;
import org.qbynet.chat.service.EventService;
import org.qbynet.chat.service.LinkPreviewService;
import org.qbynet.chat.service.MessageService;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.exception.Forbidden;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
    EventService eventService;

    @Resource
    MemberRepository memberRepository;

    @Resource
    ConversationRepository conversationRepository;

    @Resource
    StickerRepository stickerRepository;

    @Resource
    LinkPreviewService linkPreviewService;

    @Resource
    UserService userService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource(name = "messagesQueue")
    Queue messagesQueue;

    @Resource(name = "messageReadQueue")
    Queue messageReadQueue;

    @Resource
    LanguageDetector languageDetector;

    @Override
    public Message send(Message source) {
        Message message = messageRepository.save(source);
        rabbitTemplate.convertAndSend(messagesQueue.getName(), message);
        return message;
    }

    @Override
    public Message send(@NotNull SendMessageDTO dto) {
        User user = userService.currentUser();
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
        if (conversation.getAutoDeleteTimer() != null) {
            message.setExpiresAt(Instant.now().plus(conversation.getAutoDeleteTimer()));
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
    public void delete(@NotNull Message message) {
        messageRepository.delete(message);
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
        // quited, muted or banned
        if (member.isMuted() || member.isQuitOrBanned()) {
            return false;
        }
        return member.getPermissions() != null && member.getPermissions().contains(MemberPermission.SEND_TEXT_MESSAGE);
    }

    @Override
    public void markAsRead(@NotNull List<String> messageIds, User user) {
        List<Message> messages = messageIds.stream().map(messageId -> messageRepository.findById(messageId).orElse(null)).filter(Objects::nonNull).filter(message -> !message.getSender().getUser().equals(user)).toList();
        readRepository.saveAll(messages.stream().map(it -> {
            Member member = memberRepository.findByUserAndConversation(user, it.getConversation()).orElse(null);
            if (member == null) {
                return null;
            }
            // todo push to each user
            return Read.builder().message(it).member(member).build();
        }).filter(Objects::nonNull).toList());
    }

    @Override
    public void markAsRead(List<String> messages) {
        User user = userService.currentUser();
        rabbitTemplate.convertAndSend(messageReadQueue.getName(), ReadMessage.builder().messages(messages).user(user).build());
    }

    @Scheduled(cron = "0 0 * * * *")
    private void autoDeleteExpiredMessages() {
        messageRepository.deleteAllByExpiresAtLessThan(Instant.now());
    }

    @Override
    public Message findMessageById(String id) {
        return messageRepository.findById(id).orElse(null);
    }

    @Override
    public Message editMessage(@NotNull Message message, String content, String sticker, List<String> medias, boolean linkPreview) {
        message.setEditAt(Instant.now());
        message.setContent(content);
        if (medias != null && !medias.isEmpty()) {
            message.setMedias(mediaRepository.findAllById(medias));
        }
        if (sticker != null) {
            message.setSticker(stickerRepository.findById(sticker).orElse(null));
        }
        if (linkPreview) {
            message.setLinkPreview(linkPreviewService.fromText(message.getContent()));
        } else {
            message.setLinkPreview(null);
        }
        Optional<LdLocale> lang = languageDetector.detect(message.getContent());
        if (lang.isPresent()) {
            message.setLanguage(lang.get().getLanguage());
        }
        Message saved = messageRepository.save(message);
        rabbitTemplate.convertAndSend(messagesQueue.getName(), saved);
        return saved;
    }


    @Override
    public Page<Message> fetchMessages(Conversation conversation, User user, Pageable pageable) {
        Member member = memberRepository.findByUserAndConversation(user, conversation).orElse(null);
        if (member == null || !member.hasViewPermission()) return null;
        return messageRepository.findAllByConversationOrderBySentAtDesc(conversation, pageable);
    }

    @Override
    public Page<Message> fetchMessages(Conversation conversation, Message since, User user, Pageable pageable) {
        Member member = memberRepository.findByUserAndConversation(user, conversation).orElse(null);
        if (member == null || !member.hasViewPermission()) return null;
        return messageRepository.findAllByIdGreaterThanAndConversationOrderBySentAtDesc(since.getId(), conversation, pageable);
    }

    @Override
    public MessageVO toMessageVO(@NotNull Message message, User user) {
        User senderUser = message.getSender().getUser();
        return MessageVO.builder(message)
            .myself(senderUser.equals(user))
            .bot(userService.isBot(senderUser))
            .readCount(readRepository.countByMessage(message))
            .build();
    }

    @Override
    public void clearHistory(@NotNull Conversation conversation) {
        log.info("Clear history for {}", conversation.getId());
        messageRepository.deleteAllByConversation(conversation);
        eventService.clearHistory(conversation);
    }

    @Override
    public void delete(@NotNull DeleteMessageDTO input) {
        List<Message> deleted = messageRepository.findAllById(input.getMessages());
        messageRepository.deleteAll(deleted);
        eventService.deleteMessages(deleted);
    }

    @Override
    public Message editMessage(@NotNull EditMessageDTO dto) {
        User user = userService.currentUser();
        Message message = messageRepository.findById(dto.getMessage()).orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if (!message.isBelongsTo(user)) {
            throw new Forbidden("No Permission");
        }
        return this.editMessage(message, dto.getContent(), dto.getSticker(), dto.getMedias(), dto.isLinkPreview());
    }
}
