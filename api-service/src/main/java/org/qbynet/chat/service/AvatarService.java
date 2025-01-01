package org.qbynet.chat.service;

import org.qbynet.chat.entity.Avatar;
import org.qbynet.chat.entity.Conversation;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;

import java.util.List;

public interface AvatarService {
    /**
     * Find the avatar
     *
     * @param id avatar id
     * @return the avatar
     */
    Avatar getAvatar(String id);

    /**
     * Find the first avatar for user
     *
     * @param user the user
     * @return the avatar
     */
    Avatar getLatestAvatar(User user);

    /**
     * Find the first avatar for conversation
     *
     * @param conversation the conversation
     * @return the avatar
     */
    Avatar getLatestAvatar(Conversation conversation);

    /**
     * Add an avatar for a user
     *
     * @param media the avatar media
     * @param user  the user
     * @return the saved avatar
     */
    Avatar addAvatar(Media media, User user);

    /**
     * Add an avatar for a conversation
     *
     * @param media        the avatar media
     * @param conversation the conversation
     * @return the saved avatar
     */
    Avatar addAvatar(Media media, Conversation conversation);

    /**
     * Remove an avatar
     *
     * @param avatar the avatar
     */
    void removeAvatar(Avatar avatar);

    boolean isAvatarBelongsTo(Avatar avatar, User user);

    boolean isAvatarBelongsTo(Avatar avatar, Conversation conversation);

    List<Avatar> getAllAvatars(User user);

    List<Avatar> getAllAvatars(Conversation conversation, User self);
}
