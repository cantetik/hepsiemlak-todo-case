package com.hepsiemlak.todo.service;

import com.hepsiemlak.todo.aspect.Log;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Log
    public String registerUser(UserRequest request) {
        Optional<User> optional = userRepository.findByUsername(request.getUsername());

        if (optional.isPresent()) {
            throw new ConflictException("User already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setLastModifiedDate(System.currentTimeMillis());
        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Log
    public Map<String, String> loginUser(UserRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Bad credentials");
        }

        String accessToken = jwtUtil.generateToken(request.getUsername(), user.getLastModifiedDate());
        String refreshToken = UUID.randomUUID().toString();

        tokenRepository.save(Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(request.getUsername())
                .expirationDate(System.currentTimeMillis() + refreshExpiration)
                .build());

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return response;
    }

    @Log
    public Map<String, String> refreshToken(RefreshTokenRequest request) {
        Token token = tokenRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));

        if (token.getExpirationDate() < System.currentTimeMillis()) {
            throw new UnauthorizedException("Refresh token expired");
        }

        if (!token.getAccessToken().equals(request.getAccessToken())) {
            throw new UnauthorizedException("Access token is not valid");
        }

        User user = userRepository.findByUsername(token.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String username = jwtUtil.extractUsernameByToken(request.getAccessToken());
        if (!username.equals(user.getUsername())) {
            throw new NotFoundException("User not found");
        }

        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getLastModifiedDate());
        String newRefreshToken = UUID.randomUUID().toString();

        tokenRepository.save(Token.builder()
                .id(token.getId())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .username(username)
                .expirationDate(System.currentTimeMillis() + refreshExpiration)
                .build());

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);

        return response;
    }

    @Log
    public void changePassword(PasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Bad credentials");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastModifiedDate(System.currentTimeMillis());
        userRepository.save(user);
    }

    @Log
    public void deleteUser(String authorization) {
        String username = jwtUtil.extractUsernameByAuthorization(authorization);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.getTasks().forEach(todoRepository::deleteById);
        userRepository.deleteByUsername(username);
    }
}
