package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;
import com.example.ShortenerProject.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    @Transactional
    public ShortUrlResponse createShortUrl(ShortUrlCreateRequest request, User user) {
        if (shortUrlRepository.findByShortUrl(request.getShortUrl()).isPresent()) {
            throw new IllegalArgumentException("Short URL already exists.");
        }

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortUrl(request.getShortUrl());
        shortUrl.setOriginUrl(request.getOriginUrl());
        shortUrl.setDateOfCreating(request.getDateOfCreating());
        shortUrl.setDateOfExpiring(request.getDateOfExpiring());
        shortUrl.setUser(user);

        ShortUrl savedShortUrl = shortUrlRepository.save(shortUrl);
        return mapToResponse(savedShortUrl);
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> findAllShortUrlsByUser(User user) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> Objects.equals(url.getUser().getId(), user.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrlResponse> findByIdAndUser(long id, User user) {
        return shortUrlRepository.findById(id)
                .filter(url -> Objects.equals(url.getUser().getId(), user.getId()))
                .map(this::mapToResponse);
    }

    @Transactional
    public void deleteShortUrl(ShortUrl shortUrl) {
        shortUrlRepository.delete(shortUrl);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrlResponse> findByShortUrl(String shortUrl) {
        return shortUrlRepository.findByShortUrl(shortUrl)
                .map(this::mapToResponse);
    }

    @Transactional
    public void updateShortUrl(ShortUrlCreateRequest request, long id, User user) {
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .filter(url -> Objects.equals(url.getUser().getId(), user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Short URL does not exist."));

        shortUrl.setShortUrl(request.getShortUrl());
        shortUrl.setOriginUrl(request.getOriginUrl());
        shortUrl.setDateOfCreating(request.getDateOfCreating());
        shortUrl.setDateOfExpiring(request.getDateOfExpiring());

        ShortUrl updatedShortUrl = shortUrlRepository.save(shortUrl);
        mapToResponse(updatedShortUrl);
    }

    private ShortUrlResponse mapToResponse(ShortUrl shortUrl) {
        ShortUrlResponse response = new ShortUrlResponse();
        response.setShortUrl(shortUrl.getShortUrl());
        response.setOriginUrl(shortUrl.getOriginUrl());
        response.setDateOfCreating(shortUrl.getDateOfCreating());
        response.setDateOfExpiring(shortUrl.getDateOfExpiring());
        response.setUserName(shortUrl.getUser().getUsername());
        return response;
    }
}