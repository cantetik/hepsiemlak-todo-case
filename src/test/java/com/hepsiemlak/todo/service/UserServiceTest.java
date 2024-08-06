package com.hepsiemlak.todo.service;

import com.hepsiemlak.todo.entity.Token;
import com.hepsiemlak.todo.entity.User;
import com.hepsiemlak.todo.exception.ConflictException;
import com.hepsiemlak.todo.exception.NotFoundException;
import com.hepsiemlak.todo.exception.UnauthorizedException;
import com.hepsiemlak.todo.mapping.UserMapper;
import com.hepsiemlak.todo.model.user.PasswordRequest;
import com.hepsiemlak.todo.model.user.RefreshTokenRequest;
import com.hepsiemlak.todo.model.user.UserRequest;
import com.hepsiemlak.todo.repository.TodoRepository;
import com.hepsiemlak.todo.repository.TokenRepository;
import com.hepsiemlak.todo.repository.UserRepository;
import com.hepsiemlak.todo.util.JwtUtil;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Faker faker = new Faker();

    @InjectMocks
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenRepository tokenRepository;

    @Test
    void it_should_register_user() {
        UserRequest request = UserRequest.builder()
                .username(faker.name().name())
                .password(faker.password().toString())
                .build();

        String encodedPassword = faker.password().toString();

        User user = User.builder().id(faker.name().name()).build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        String actual = userService.registerUser(request);
        verify(userRepository, times(1)).save(user);
        assertEquals(user.getId(), actual);
    }

    @Test
    void it_should_throw_conflict_exception_when_registering_user() {
        UserRequest request = UserRequest.builder()
                .username(faker.name().name())
                .password(faker.password().toString())
                .build();

        User user = User.builder().id(faker.name().name()).build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.registerUser(request));

        verify(userRepository, never()).save(any());
        assertEquals("User already exists", exception.getMessage());
    }

    @Test
    void it_should_login_user() throws NoSuchFieldException, IllegalAccessException {
        UserRequest request = UserRequest.builder()
                .username(faker.name().name())
                .password(faker.password().toString())
                .build();

        String accessToken = faker.password().toString();

        User user = User.builder()
                .id(faker.name().name())
                .password(request.getPassword())
                .lastModifiedDate(System.currentTimeMillis())
                .build();

        Field field = UserService.class.getDeclaredField("refreshExpiration");
        field.setAccessible(true);
        field.set(userService, 2592000000L);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(request.getUsername(), user.getLastModifiedDate())).thenReturn(accessToken);
        when(tokenRepository.save(any(Token.class))).thenReturn(Token.builder().build());

        Map<String, String> actual = userService.loginUser(request);
        verify(tokenRepository, times(1)).save(any(Token.class));
        assertEquals(2, actual.size());
        assertNotNull(actual.get("accessToken"));
        assertNotNull(actual.get("refreshToken"));
    }

    @Test
    void it_should_throw_not_found_exception_when_logging_user() {
        UserRequest request = UserRequest.builder()
                .username(faker.name().name())
                .password(faker.password().toString())
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.loginUser(request));

        verify(tokenRepository, never()).save(any(Token.class));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void it_should_throw_unauthorized_exception_when_logging_user() {
        UserRequest request = UserRequest.builder()
                .username(faker.name().name())
                .password(faker.password().toString())
                .build();

        User user = User.builder()
                .id(faker.name().name())
                .password(request.getPassword())
                .lastModifiedDate(System.currentTimeMillis())
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> userService.loginUser(request));

        verify(tokenRepository, never()).save(any(Token.class));
        assertEquals("Bad credentials", exception.getMessage());
    }

    @Test
    void it_should_refresh_user() throws NoSuchFieldException, IllegalAccessException {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .accessToken(faker.idNumber().valid())
                .refreshToken(faker.idNumber().valid())
                .build();

        Token token = Token.builder()
                .username(faker.name().name())
                .accessToken(request.getAccessToken())
                .expirationDate(System.currentTimeMillis() + 2592000000L)
                .build();

        User user = User.builder()
                .username(token.getUsername())
                .lastModifiedDate(System.currentTimeMillis())
                .build();

        String accessToken = faker.password().toString();

        Field field = UserService.class.getDeclaredField("refreshExpiration");
        field.setAccessible(true);
        field.set(userService, 2592000000L);

        when(tokenRepository.findByRefreshToken(request.getRefreshToken())).thenReturn(Optional.of(token));
        when(userRepository.findByUsername(token.getUsername())).thenReturn(Optional.of(user));
        when(jwtUtil.extractUsernameByToken(request.getAccessToken())).thenReturn(user.getUsername());
        when(jwtUtil.generateToken(user.getUsername(), user.getLastModifiedDate())).thenReturn(accessToken);
        when(tokenRepository.save(any(Token.class))).thenReturn(Token.builder().build());

        Map<String, String> actual = userService.refreshToken(request);

        verify(tokenRepository, times(1)).save(any(Token.class));
        assertEquals(2, actual.size());
        assertNotNull(actual.get("accessToken"));
        assertNotNull(actual.get("refreshToken"));
    }

    @Test
    void it_should_throw_refresh_token_not_found_exception_when_refreshing_token() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .accessToken(faker.idNumber().valid())
                .refreshToken(faker.idNumber().valid())
                .build();

        when(tokenRepository.findByRefreshToken(request.getRefreshToken())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.refreshToken(request));

        verify(tokenRepository, never()).save(any(Token.class));
        assertEquals("Refresh token not found", exception.getMessage());
    }

    @Test
    void it_should_throw_refresh_token_expired_exception_when_refreshing_token() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .accessToken(faker.idNumber().valid())
                .refreshToken(faker.idNumber().valid())
                .build();

        Token token = Token.builder()
                .username(faker.name().name())
                .accessToken(request.getAccessToken())
                .expirationDate(System.currentTimeMillis() - 2592000000L)
                .build();

        when(tokenRepository.findByRefreshToken(request.getRefreshToken())).thenReturn(Optional.of(token));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> userService.refreshToken(request));

        verify(tokenRepository, never()).save(any(Token.class));
        assertEquals("Refresh token expired", exception.getMessage());
    }

    @Test
    void it_should_throw_access_token_is_not_valid_exception_when_refreshing_token() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .accessToken(faker.idNumber().valid())
                .refreshToken(faker.idNumber().valid())
                .build();

        Token token = Token.builder()
                .username(faker.name().name())
                .accessToken(faker.idNumber().valid())
                .expirationDate(System.currentTimeMillis() + 2592000000L)
                .build();

        when(tokenRepository.findByRefreshToken(request.getRefreshToken())).thenReturn(Optional.of(token));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> userService.refreshToken(request));

        verify(tokenRepository, never()).save(any(Token.class));
        assertEquals("Access token is not valid", exception.getMessage());
    }

    @Test
    void it_should_throw_user_not_found_exception_when_refreshing_token() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .accessToken(faker.idNumber().valid())
                .refreshToken(faker.idNumber().valid())
                .build();

        Token token = Token.builder()
                .username(faker.name().name())
                .accessToken(request.getAccessToken())
                .expirationDate(System.currentTimeMillis() + 2592000000L)
                .build();

        User user = User.builder()
                .username(token.getUsername())
                .lastModifiedDate(System.currentTimeMillis())
                .build();

        when(tokenRepository.findByRefreshToken(request.getRefreshToken())).thenReturn(Optional.of(token));
        when(userRepository.findByUsername(token.getUsername())).thenReturn(Optional.of(user));
        when(jwtUtil.extractUsernameByToken(request.getAccessToken())).thenReturn(faker.name().name());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.refreshToken(request));

        verify(tokenRepository, never()).save(any(Token.class));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void it_should_change_password() {
        PasswordRequest request = PasswordRequest.builder()
                .oldPassword(faker.password().toString())
                .newPassword(faker.password().toString())
                .username(faker.name().name())
                .build();

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getOldPassword())
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn(faker.password().toString());
        when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

        userService.changePassword(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void it_should_throw_user_not_found_exception_when_changing_password() {
        PasswordRequest request = PasswordRequest.builder()
                .oldPassword(faker.password().toString())
                .newPassword(faker.password().toString())
                .username(faker.name().name())
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.changePassword(request));

        verify(userRepository, never()).save(any(User.class));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void it_should_throw_bad_credentials_exception_when_changing_password() {
        PasswordRequest request = PasswordRequest.builder()
                .oldPassword(faker.password().toString())
                .newPassword(faker.password().toString())
                .username(faker.name().name())
                .build();

        User user = User.builder()
                .username(request.getUsername())
                .password(faker.password().toString())
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> userService.changePassword(request));

        verify(userRepository, never()).save(any(User.class));
        assertEquals("Bad credentials", exception.getMessage());
    }

    @Test
    void it_should_delete_user() {
        String authorization = faker.name().name();
        String username = faker.name().name();

        PasswordRequest request = PasswordRequest.builder()
                .oldPassword(faker.password().toString())
                .newPassword(faker.password().toString())
                .username(faker.name().name())
                .build();

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getOldPassword())
                .tasks(List.of(faker.name().name()))
                .build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        doNothing().when(todoRepository).deleteById(user.getTasks().get(0));
        doNothing().when(userRepository).deleteByUsername(username);

        userService.deleteUser(authorization);

        verify(userRepository, times(1)).deleteByUsername(username);
    }

    @Test
    void it_should_throw_user_not_found_exception_when_deleting_user() {
        String authorization = faker.name().name();
        String username = faker.name().name();

        PasswordRequest request = PasswordRequest.builder()
                .oldPassword(faker.password().toString())
                .newPassword(faker.password().toString())
                .username(faker.name().name())
                .build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(authorization));

        verify(userRepository, never()).deleteByUsername(username);
        assertEquals("User not found", exception.getMessage());
    }

}