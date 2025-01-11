package com.example.ShortenerProject.shortUrl;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ShortUrlCreator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_URL_LENGTH = 8;

    /**
     * Generate a random short URL.
     *
     * @return Generated short URL
     */
    public String generateShortUrl() {
        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder();
        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            shortUrl.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return shortUrl.toString();
    }

    /**
     * Checks the validity of the original URL.
     *
     * @param url Original URL
     * @return true if the URL is valid, otherwise false
     */
    public boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
