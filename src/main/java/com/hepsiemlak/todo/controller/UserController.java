package com.hepsiemlak.todo.controller;

import com.hepsiemlak.todo.aspect.Log;
import com.hepsiemlak.todo.model.user.PasswordRequest;
import com.hepsiemlak.todo.model.user.RefreshTokenRequest;
import com.hepsiemlak.todo.model.user.UserRequest;
import com.hepsiemlak.todo.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Log
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid UserRequest request) {
        String userId = userService.registerUser(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Id", userId);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @Log
    @PostMapping("/login")
    public ResponseEntity<Void> loginUser(@RequestBody @Valid UserRequest request) {
        Map<String, String> tokens = userService.loginUser(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Token", tokens.get("accessToken"));
        headers.add("Refresh-Token", tokens.get("refreshToken"));
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @Log
    @PostMapping("/refresh-token")
    public ResponseEntity<Void> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        Map<String, String> tokens = userService.refreshToken(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Token", tokens.get("accessToken"));
        headers.add("Refresh-Token", tokens.get("refreshToken"));
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @Log
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "Authorization")
    public void changePassword(@RequestBody @Valid PasswordRequest request) {
        userService.changePassword(request);
    }

    @Log
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "Authorization")
    public void deleteUser(@RequestHeader("Authorization") String authorization) {
        userService.deleteUser(authorization);
    }
}
