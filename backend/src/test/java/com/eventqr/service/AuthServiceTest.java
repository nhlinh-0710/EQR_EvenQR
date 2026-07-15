package com.eventqr.service;

import com.eventqr.model.Account;
import com.eventqr.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account("Nguyen Van A", "test@example.com", "password123", "0123456789", "user");
    }

    @Test
    void register_whenAccountIsNull_shouldReturnFalse() {
        boolean result = authService.register(null);
        assertFalse(result);
        verifyNoInteractions(accountRepo);
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturnFalse() {
        when(accountRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));

        boolean result = authService.register(testAccount);

        assertFalse(result);
        verify(accountRepo).findByEmail("test@example.com");
        verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void register_whenValidAccount_shouldEncodePasswordAndSave() {
        when(accountRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = authService.register(testAccount);

        assertTrue(result);
        verify(accountRepo).save(accountCaptor.capture());
        Account saved = accountCaptor.getValue();

        assertNotEquals("password123", saved.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("password123", saved.getPassword()));
        assertEquals("user", saved.getRole());
        assertTrue(saved.getActive());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void register_whenRoleIsNull_shouldDefaultToUser() {
        testAccount.setRole(null);
        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(testAccount);

        verify(accountRepo).save(accountCaptor.capture());
        assertEquals("user", accountCaptor.getValue().getRole());
    }

    @Test
    void register_whenRoleIsEmpty_shouldDefaultToUser() {
        testAccount.setRole("");
        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(testAccount);

        verify(accountRepo).save(accountCaptor.capture());
        assertEquals("user", accountCaptor.getValue().getRole());
    }

    @Test
    void login_whenEmailNotFound_shouldReturnEmpty() {
        when(accountRepo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<Account> result = authService.login("unknown@example.com", "any");

        assertTrue(result.isEmpty());
    }

    @Test
    void login_whenPasswordWrong_shouldReturnEmpty() {
        when(accountRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));

        Optional<Account> result = authService.login("test@example.com", "wrongpassword");

        assertTrue(result.isEmpty());
    }

    @Test
    void login_whenCredentialsValid_shouldReturnAccount() {
        String rawPassword = "correctPassword";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        testAccount.setPassword(encodedPassword);
        when(accountRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));

        Optional<Account> result = authService.login("test@example.com", rawPassword);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }
}
