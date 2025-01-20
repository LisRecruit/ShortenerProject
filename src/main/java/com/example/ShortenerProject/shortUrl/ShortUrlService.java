package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.exception.CantBeNullException;
import com.example.ShortenerProject.exception.EntityNotFoundException;
import com.example.ShortenerProject.exception.InvalidOriginUrlException;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlStatsResponse;
import com.example.ShortenerProject.user.User;
import com.example.ShortenerProject.user.UserRepository;
import com.example.ShortenerProject.utils.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;
    private final ShortUrlCreator shortUrlCreator;
    private final ShortUrlMapper shortUrlMapper;
    private final Validator urlValidator;
    @Autowired
    public ShortUrlService(ShortUrlRepository shortUrlRepository, UserRepository userRepository, ShortUrlCreator shortUrlCreator
    ,ShortUrlMapper shortUrlMapper, Validator urlValidator) {
        this.shortUrlRepository = shortUrlRepository;
        this.userRepository = userRepository;
        this.shortUrlCreator = shortUrlCreator;
        this.shortUrlMapper = shortUrlMapper;
        this.urlValidator = urlValidator;
    }

    @Transactional
    public ShortUrlResponse createShortUrl(ShortUrlCreateRequest request) {

        if (!urlValidator.isValidUrl(request.getOriginUrl())) {
            throw new InvalidOriginUrlException("Invalid origin URL: " + request.getOriginUrl());
        }
        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new EntityNotFoundException(User.class,"id", request.getUser()));

        ShortUrl shortUrl = shortUrlMapper.toEntity(request);
        shortUrl.setShortUrl(shortUrlCreator.generateUniqueShortUrl());
        shortUrl.setUser(user);

        ShortUrl savedShortUrl = shortUrlRepository.save(shortUrl);
        return shortUrlMapper.toResponse(savedShortUrl);
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> findAllShortUrlsByUser(User user) {
        if (user == null) {
            throw new CantBeNullException(User.class);
        }
        return shortUrlRepository.findByUserId(user.getId()).stream()
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
    public ShortUrlResponse findShortUrlById(long id) {
        return shortUrlRepository.findById(id)
                .map(shortUrlMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Short URL with ID " + id + " does not exist."));
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> findByUser(User user) {
        if (user == null || user.getId() == 0) {
            throw new IllegalArgumentException("Invalid user or user ID");
        }
        List<ShortUrl> shortUrls = shortUrlRepository.findByUserId(user.getId());
        if (shortUrls.isEmpty()) {
            throw new EntityNotFoundException("No Short URLs found for the user with ID " + user.getId());
        }
        return shortUrls.stream()
                .map(shortUrlMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrlResponse> findByIdAndUser(long id, User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Invalid user or user ID");
        }
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
        if (shortUrl == null || shortUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Short URL must not be null or empty");
        }
        return shortUrlRepository.findByShortUrl(shortUrl)
                .map(shortUrlMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrlStatsResponse> getShortUrlStats(String shortUrl, User user) {
        if (shortUrl == null || shortUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Short URL must not be null or empty");
        }

        return shortUrlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getUser().getId() == user.getId())
                .map(url -> new ShortUrlStatsResponse(url.getShortUrl(), url.getCountOfTransition()));
    }


    @Transactional
    public ShortUrlResponse  updateShortUrl(ShortUrlCreateRequest request, long id, User user) {
        if (request.getOriginUrl() == null || request.getOriginUrl().trim().isEmpty()) {
            throw new InvalidOriginUrlException("Origin URL cannot be null or empty");
        }
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .filter(url -> Objects.equals(url.getUser().getId(), user.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Short URL with ID " + id + " does not exist or does not belong to the user."));

        if (!shortUrl.getOriginUrl().equals(request.getOriginUrl())) {
            shortUrl.setShortUrl(shortUrlCreator.generateUniqueShortUrl());
        }

        shortUrl.setOriginUrl(request.getOriginUrl());
        shortUrl.setDateOfCreating(request.getDateOfCreating());
        shortUrl.setDateOfExpiring(request.getDateOfExpiring());

        ShortUrl updatedShortUrl = shortUrlRepository.save(shortUrl);
        return shortUrlMapper.toResponse(updatedShortUrl);
    }

    @Transactional
    public Optional<ShortUrl> findAndRedirect(String shortUrl) {
        Optional<ShortUrl> foundUrl = shortUrlRepository.findByShortUrl(shortUrl);
        foundUrl.ifPresent(url -> incrementTransitionCount(shortUrl));
        return foundUrl;
    }
    @Transactional(readOnly = true)
    public Optional<String> findOriginalUrl(String shortUrl, User user) {
        if (user == null || user.getId() == 0) {
            return Optional.empty();
        }
        return shortUrlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getUser().getId() == user.getId())
                .map(ShortUrl::getOriginUrl);
    }
    private void incrementTransitionCount(String shortUrl) {
        ShortUrl shortUrlEntity = shortUrlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new EntityNotFoundException("Short Url not found"));
        shortUrlEntity.setCountOfTransition(shortUrlEntity.getCountOfTransition() + 1);
        shortUrlRepository.save(shortUrlEntity);

    }

}