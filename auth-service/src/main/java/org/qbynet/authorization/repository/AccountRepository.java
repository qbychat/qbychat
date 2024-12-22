package org.qbynet.authorization.repository;

import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRolesContains(Role role);
}
