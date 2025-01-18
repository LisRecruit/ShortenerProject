package com.example.ShortenerProject.shortUrl;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Data
public class ShortUrlCreator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_URL_LENGTH = 8;
    private final ShortUrlRepository shortUrlRepository;

    public ShortUrlCreator(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

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
     * Generate unique short URL
     *
     * @return Unique short URL
     */
    public String generateUniqueShortUrl() {
        String shortUrl;
        do {
            shortUrl = generateShortUrl();
        } while (shortUrlRepository.existsByShortUrl(shortUrl));
        return shortUrl;
    }

}
