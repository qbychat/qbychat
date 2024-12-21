package org.qbynet.chat.repository;

import org.qbynet.chat.entity.Contact;
import org.qbynet.chat.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
    Optional<Contact> findByOwnerAndTarget(User owner, User target);

    boolean existsByOwnerAndTarget(User owner, User target);
}
