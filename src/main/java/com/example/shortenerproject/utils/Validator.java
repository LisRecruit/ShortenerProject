package com.example.shortenerproject.utils;

import com.example.shortenerproject.shorturl.ShortUrl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class Validator {
    private final RestTemplate restTemplate;
    public Validator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final String PASSWORD_PATTERN =  "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    public boolean isValidUrl(String url) {
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(url, Void.class);

            return response.getStatusCode().equals(HttpStatus.OK);
        } catch (Exception e) {
            return false;
        }
    }
    public boolean isDateValid(ShortUrl shortUrl){
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime expireDate = LocalDateTime.parse(shortUrl.getDateOfExpiring(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (today.isAfter(expireDate)){
            return false;
        }
        return true;
    }
}
