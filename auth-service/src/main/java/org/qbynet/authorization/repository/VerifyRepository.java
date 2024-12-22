package org.qbynet.authorization.repository;

import org.qbynet.authorization.entity.Verify;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerifyRepository extends KeyValueRepository<Verify, String> {
    Optional<Verify> findByEmailAndToken(String email, String token);

    boolean existsByEmail(String email);
}
