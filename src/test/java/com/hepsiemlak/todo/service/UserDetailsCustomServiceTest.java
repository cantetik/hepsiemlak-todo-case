package com.hepsiemlak.todo.service;

import com.hepsiemlak.todo.entity.User;
import com.hepsiemlak.todo.exception.NotFoundException;
import com.hepsiemlak.todo.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsCustomServiceTest {

    private static final Faker faker = new Faker();

    @InjectMocks
    private UserDetailsCustomService userDetailsCustomService;

    @Mock
    private UserRepository userRepository;


    @Test
    void it_should_load_user_by_username() {
        String username = faker.name().name();

        User user = User.builder()
                .id(faker.name().name())
                .username(username)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails actual = userDetailsCustomService.loadUserByUsername(username);
        verify(userRepository, times(1)).findByUsername(username);
        assertEquals(user.getUsername(), actual.getUsername());
    }

    @Test
    void it_should_throw_not_found_exception_when_loading_user_by_username() {
        String username = faker.name().name();

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userDetailsCustomService.loadUserByUsername(username));
    }
}