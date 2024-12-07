package org.qbynet.chat.repository;

import org.qbynet.chat.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByRemoteId(String remoteId);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findAllByUsernameStartsWithIgnoreCase(String username, Pageable pageable);
}
