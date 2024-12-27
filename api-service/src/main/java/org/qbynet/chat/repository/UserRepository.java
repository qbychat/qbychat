package org.qbynet.chat.repository;

import org.qbynet.chat.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByRemoteId(String remoteId);

    Mono<User> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);

    Flux<User> findAllByUsernameStartsWithIgnoreCase(String username, Pageable pageable);

    Flux<User> findAllByNicknameContainingIgnoreCase(String nickname, Pageable pageable);
}
