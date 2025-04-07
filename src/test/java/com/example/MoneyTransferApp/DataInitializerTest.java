package com.example.MoneyTransferApp;

import com.example.MoneyTransferApp.config.DataInitializer;
import com.example.MoneyTransferApp.repository.UserRepository;
import com.example.MoneyTransferApp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void shouldSkipInitializationIfUsersExist() throws Exception {
        when(userRepository.count()).thenReturn(5L);

        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        verify(userRepository, times(1)).count();
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldInitializeDataIfNoUsersExist() throws Exception {
        when(userRepository.count()).thenReturn(0L);

        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).saveAll(anyList());
    }
}
