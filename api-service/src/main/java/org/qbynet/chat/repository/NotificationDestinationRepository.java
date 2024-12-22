package org.qbynet.chat.repository;

import org.qbynet.chat.entity.NotificationDestination;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDestinationRepository extends KeyValueRepository<NotificationDestination, String> {
    boolean existsByUser(String user);

    List<NotificationDestination> findAllByUser(String user);
}
