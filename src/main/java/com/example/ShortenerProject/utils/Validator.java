package com.example.ShortenerProject.utils;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class Validator {
    private static final String PASSWORD_PATTERN =  "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }
}
