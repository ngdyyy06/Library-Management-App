package com.library.management.service;

import com.library.management.entity.User;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AuthService(
            UserRepository userRepository,
            PasswordService passwordService
    ) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public User login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Username not found"
                        ));

        boolean passwordMatches;

        // Password đã được mã hóa BCrypt
        if (user.getPassword().startsWith("$2a$")
                || user.getPassword().startsWith("$2b$")) {

            passwordMatches = passwordService.matches(
                    password,
                    user.getPassword()
            );

        } else {
            // Password cũ đang lưu dạng plain text
            passwordMatches = password.equals(
                    user.getPassword()
            );

            // Nếu đăng nhập đúng thì tự động mã hóa password
            if (passwordMatches) {
                user.setPassword(
                        passwordService.encode(password)
                );

                userRepository.save(user);
            }
        }

        if (!passwordMatches) {
            throw new RuntimeException(
                    "Incorrect password"
            );
        }

        if ("INACTIVE".equals(user.getStatus())) {
            throw new RuntimeException(
                    "User is inactive"
            );
        }

        return user;
    }

    public String getUserRole(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        return user.getRole().getName();
    }
}