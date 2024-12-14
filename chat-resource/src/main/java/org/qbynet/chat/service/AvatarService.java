package org.qbynet.chat.service;

import org.qbynet.chat.entity.Avatar;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;

import java.util.List;

public interface AvatarService {
    Avatar getAvatar(String id);

    Avatar getLatestAvatar(User user);

    Avatar getLatestAvatar(Conversation conversation);

    Avatar addAvatar(Media media, User user);

    Avatar addAvatar(Media media, Conversation conversation);

    void removeAvatar(Avatar avatar);

    boolean isAvatarBelongsTo(Avatar avatar, User user);

    boolean isAvatarBelongsTo(Avatar avatar, Conversation conversation);

    List<Avatar> findAllAvatars(User user);

    List<Avatar> findAllAvatars(Conversation conversation);
}
