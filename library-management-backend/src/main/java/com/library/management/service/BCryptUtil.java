package com.library.management.service;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtil {

    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean matches(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}