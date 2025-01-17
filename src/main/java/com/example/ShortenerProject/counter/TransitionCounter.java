package com.example.ShortenerProject.counter;

import com.example.ShortenerProject.shortUrl.ShortUrl;
import com.example.ShortenerProject.shortUrl.ShortUrlRepository;
import org.springframework.stereotype.Component;

@Component
public class TransitionCounter {
    private final ShortUrlRepository shortUrlRepository;

    public TransitionCounter(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    public ShortUrl incrementTransitionCount(String shortUrl) {
        ShortUrl shortUrlEntity = shortUrlRepository.findAll()
                .stream()
                .filter(url -> url.getShortUrl().equals(shortUrl))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Short Url not found"));

        shortUrlEntity.setCountOfTransition(shortUrlEntity.getCountOfTransition() + 1);
        shortUrlRepository.save(shortUrlEntity);

        return shortUrlEntity;

    }

}
