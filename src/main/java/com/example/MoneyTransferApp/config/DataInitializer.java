package com.example.MoneyTransferApp.config;

import com.example.MoneyTransferApp.entity.Account;
import com.example.MoneyTransferApp.entity.User;
import com.example.MoneyTransferApp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("Starting data initialization...");
            if (userRepository.count() == 0) {
                initializeDatabase();
            } else {
                log.info("Data already initialized. Skipping...");
            }
            log.info("Data initialization completed successfully");
        };
    }

    @Transactional
    protected void initializeDatabase() {
        List<User> users = Arrays.asList(
                createUserWithAccount("john_doe", "John Doe", "john@example.com",
                        "1000000001", new BigDecimal("5000.00")),
                createUserWithAccount("jane_smith", "Jane Smith", "jane@example.com",
                        "1000000002", new BigDecimal("7500.00")),
                createUserWithAccount("alice_johnson", "Alice Johnson", "alice@example.com",
                        "1000000003", new BigDecimal("3000.00")),
                createUserWithAccount("bob_brown", "Bob Brown", "bob@example.com",
                        "1000000004", new BigDecimal("4200.00")),
                createUserWithAccount("charlie_davis", "Charlie Davis", "charlie@example.com",
                        "1000000005", new BigDecimal("6100.00"))
        );

        userRepository.saveAll(users);
        log.info("Created {} user accounts with their respective accounts", users.size());
    }

    private User createUserWithAccount(String username, String fullName, String email,
                                       String accountNumber, BigDecimal initialBalance) {
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountName(fullName + "'s Account")
                .balance(initialBalance)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .username(username)
                .fullName(fullName)
                .email(email)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user.setAccount(account);
        account.setUser(user);

        return user;
    }
}