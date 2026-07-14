package com.eventqr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eventqr.model.Account;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
}
