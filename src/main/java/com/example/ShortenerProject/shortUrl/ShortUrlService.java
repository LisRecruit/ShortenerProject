package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.user.User;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    @Transactional
    public String createShortUrl(ShortUrlCreateRequest request, User user) {

        if (request.getShortUrl() == null || request.getShortUrl().isEmpty()) {
            return "Short URL cannot be empty";
        }

        if (shortUrlRepository.findByShortUrl(request.getShortUrl()).isPresent()) {
            return "Short URL already exists";
        }

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortUrl(request.getShortUrl());
        shortUrl.setOriginUrl(request.getOriginUrl());
        shortUrl.setDateOfCreating(request.getDateOfCreating());
        shortUrl.setDateOfExpiring(request.getDateOfExpiring());
        shortUrl.setCountOfTransition(0);
        shortUrl.setUser(user);

        shortUrlRepository.save(shortUrl);
        return "Short URL created successfully";
    }

    public List<ShortUrl> getAllShortUrlsByUser(User user) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> url.getUser().getId() == user.getId())
                .toList();
    }

    public Optional<ShortUrl> findByIdAndUser(long id, User user) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> url.getId() == id && url.getUser().getId() == user.getId())
                .findFirst();
    }

    @Transactional
    public void deleteShortUrl(long id, User user) {
        shortUrlRepository.findAll().stream()
                .filter(url -> url.getId() == id && url.getUser().getId() == user.getId())
                .findFirst()
                .ifPresent(shortUrlRepository::delete);
    }

    public Optional<ShortUrl> findByShortUrl(String shortUrl) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> url.getShortUrl().equals(shortUrl))
                .findFirst();
    }

    @Transactional
    public ShortUrl updateShortUrl(ShortUrl shortUrl) {
        return shortUrlRepository.save(shortUrl);
    }
}