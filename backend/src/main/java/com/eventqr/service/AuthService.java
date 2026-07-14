package com.eventqr.service;

import com.eventqr.model.Account;
import com.eventqr.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AccountRepository accountRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public boolean register(Account account) {
        if (account == null) {
            return false;
        }
        if (accountRepo.findByEmail(account.getEmail()).isPresent()) {
            return false;
        }

        if (account.getRole() == null || account.getRole().isEmpty()) {
            account.setRole("user");
        }
        if (account.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        account.setActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        accountRepo.save(account);
        logger.info("Registered user: {} with role: {}", account.getEmail(), account.getRole());
        return true;
    }

    public Optional<Account> login(String email, String password) {
        Optional<Account> accountOpt = accountRepo.findByEmail(email);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (passwordEncoder.matches(password, account.getPassword())) {
                return Optional.of(account);
            }
        }
        return Optional.empty();
    }
}
