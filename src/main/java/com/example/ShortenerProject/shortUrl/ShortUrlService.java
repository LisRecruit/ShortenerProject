package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlStatsResponse;
import com.example.ShortenerProject.user.User;
import com.example.ShortenerProject.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;
    private final ShortUrlCreator shortUrlCreator;
    private final ShortUrlMapper shortUrlMapper;
    @Autowired
    public ShortUrlService(ShortUrlRepository shortUrlRepository, UserRepository userRepository, ShortUrlCreator shortUrlCreator
    ,ShortUrlMapper shortUrlMapper) {
        this.shortUrlRepository = shortUrlRepository;
        this.userRepository = userRepository;
        this.shortUrlCreator = shortUrlCreator;
        this.shortUrlMapper = shortUrlMapper;
    }

    @Transactional
    public ShortUrlResponse createShortUrl(ShortUrlCreateRequest request) {
        if (!shortUrlCreator.isValidUrl(request.getOriginUrl())) {
            throw new IllegalArgumentException("Invalid origin URL: " + request.getOriginUrl());
        }
        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUser()));

        ShortUrl shortUrl = shortUrlMapper.toEntity(request);
        shortUrl.setShortUrl(shortUrlCreator.generateUniqueShortUrl());
        shortUrl.setUser(user);

        ShortUrl savedShortUrl = shortUrlRepository.save(shortUrl);
        return shortUrlMapper.toResponse(savedShortUrl);
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> findAllShortUrlsByUser(User user) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> Objects.equals(url.getUser().getId(), user.getId()))
                .map(shortUrlMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> findAllShortUrls() {
        return shortUrlRepository.findAll().stream()
                .map(shortUrlMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Optional<ShortUrlResponse> findShortUrlById(long id) {
        return shortUrlRepository.findById(id)
                .map(shortUrlMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> findByUser(User user) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> Objects.equals(url.getUser().getId(), user.getId()))
                .map(shortUrlMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrlResponse> findByIdAndUser(long id, User user) {
        return shortUrlRepository.findById(id)
                .filter(shortUrl -> shortUrl.getUser().getId() == user.getId())
                .map(shortUrlMapper::toResponse);
    }

    @Transactional
    public void deleteShortUrl(Long id) {
        shortUrlRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrlResponse> findByShortUrl(String shortUrl) {
        return shortUrlRepository.findByShortUrl(shortUrl)
                .map(shortUrlMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrlStatsResponse> getShortUrlStats(String shortUrl, User user) {
        Optional<ShortUrl> foundUrl = shortUrlRepository.findByShortUrl(shortUrl);

        if (foundUrl.isPresent() && foundUrl.get().getUser().getId() == user.getId()) {
            ShortUrl url = foundUrl.get();
            return Optional.of(new ShortUrlStatsResponse(url.getShortUrl(), url.getCountOfTransition()));
        }

        return Optional.empty();
    }


    @Transactional
    public ShortUrlResponse  updateShortUrl(ShortUrlCreateRequest request, long id, User user) {
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .filter(url -> Objects.equals(url.getUser().getId(), user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Short URL does not exist."));

        shortUrl.setShortUrl(shortUrlCreator.generateUniqueShortUrl());
        shortUrl.setOriginUrl(request.getOriginUrl());
        shortUrl.setDateOfCreating(request.getDateOfCreating());
        shortUrl.setDateOfExpiring(request.getDateOfExpiring());

        ShortUrl updatedShortUrl = shortUrlRepository.save(shortUrl);
        return shortUrlMapper.toResponse(updatedShortUrl);
    }

    @Transactional
    public Optional<ShortUrl> findAndRedirect(String shortUrl) {
        Optional<ShortUrl> foundUrl = shortUrlRepository.findByShortUrl(shortUrl);
        if (foundUrl.isPresent()) {
            ShortUrl url = foundUrl.get();
            url.setCountOfTransition(url.getCountOfTransition() + 1);
            shortUrlRepository.save(url);
        }
        return foundUrl;
    }
    @Transactional(readOnly = true)
    public Optional<String> findOriginalUrl(String shortUrl, User user) {
        return shortUrlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getUser().getId() == user.getId())
                .map(ShortUrl::getOriginUrl);
    }

}