package com.hepsiemlak.todo.service;

import com.hepsiemlak.todo.entity.User;
import com.hepsiemlak.todo.exception.NotFoundException;
import com.hepsiemlak.todo.model.user.CustomUserDetails;
import com.hepsiemlak.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsCustomService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optional = userRepository.findByUsername(username);
        if (optional.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        User user = optional.get();
        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>(),
                user.getLastModifiedDate());
    }
}
