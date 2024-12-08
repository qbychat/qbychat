package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Notification;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends KeyValueRepository<Notification, String> {
}
