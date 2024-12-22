package org.qbynet.chat.service;

import org.qbynet.chat.entity.JoinRequest;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.Notification;
import org.qbynet.chat.entity.User;

import java.util.List;

public interface NotificationService {
    void createNotification(Message message);

    void createNotification(JoinRequest joinRequest);

    boolean hasNotifications(User user);

    /**
     * Fetch notifications
     *
     * @param user the user
     * @return a list of notifications
     */
    List<Notification> fetch(User user);

}
