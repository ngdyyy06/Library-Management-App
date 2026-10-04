package com.library.management.service;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

    public String encode(String password) {
        return BCrypt.hashpw(
                password,
                BCrypt.gensalt()
        );
    }

    public boolean matches(
            String rawPassword,
            String encodedPassword) {

        return BCrypt.checkpw(
                rawPassword,
                encodedPassword
        );
    }
}