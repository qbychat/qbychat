package org.qbynet.chat.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Avatar;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.repository.AvatarRepository;
import org.qbynet.chat.service.AvatarService;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.util.List;

@Log4j2
@Service
public class AvatarServiceImpl implements AvatarService {
    @Resource
    AvatarRepository avatarRepository;

    @Override
    public Avatar getAvatar(String id) {
        return avatarRepository.findById(id).orElse(null);
    }

    @Override
    public Avatar getLatestAvatar(User user) {
        return avatarRepository.findFirstByUser(user).orElse(null);
    }

    @Override
    public Avatar getLatestAvatar(Conversation conversation) {
        return avatarRepository.findFirstByConversation(conversation).orElse(null);
    }

    @Override
    public Avatar addAvatar(@NotNull Media media, User user) {
        Avatar avatar = new Avatar();
        avatar.setUser(user);
        if (!MimeType.valueOf("image/*").isCompatibleWith(MimeType.valueOf(media.getContentType()))) {
            throw new IllegalArgumentException("Unsupported image type: " + media.getContentType());
        }
        avatar.setMedia(media);
        log.info("Add avatar for user {} (media: {})", user.getNickname(), media.getName());
        return avatarRepository.save(avatar);
    }

    @Override
    public Avatar addAvatar(@NotNull Media media, Conversation conversation) {
        Avatar avatar = new Avatar();
        avatar.setConversation(conversation);
        if (!MimeType.valueOf("image/*").isCompatibleWith(MimeType.valueOf(media.getContentType()))) {
            throw new IllegalArgumentException("Unsupported image type: " + media.getContentType());
        }
        avatar.setMedia(media);
        log.info("Add avatar for conversation {} (media: {})", conversation.getName(), media.getName());
        return avatarRepository.save(avatar);
    }

    @Override
    public void removeAvatar(Avatar avatar) {
        avatarRepository.delete(avatar);
    }

    @Override
    public boolean isAvatarBelongsTo(Avatar avatar, User user) {
        if (avatar == null) return false;
        return avatar.getUser().equals(user);
    }

    @Override
    public boolean isAvatarBelongsTo(Avatar avatar, Conversation conversation) {
        if (avatar == null) return false;
        return avatar.getConversation().getId().equals(conversation.getId());
    }

    @Override
    public List<Avatar> getAllAvatars(User user) {
        return avatarRepository.findAllByUser(user);
    }

    @Override
    public List<Avatar> getAllAvatars(Conversation conversation) {
        return avatarRepository.findAllByConversation(conversation);
    }
}
