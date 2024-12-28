package org.qbynet.chat.repository;

import org.qbynet.chat.entity.AuditLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends ReactiveMongoRepository<AuditLog, String> {
}
