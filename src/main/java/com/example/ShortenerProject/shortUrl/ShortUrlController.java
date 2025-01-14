package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/short-urls")
public class ShortUrlController {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlCreator shortUrlCreator;

    public ShortUrlController(ShortUrlRepository shortUrlRepository, ShortUrlCreator shortUrlCreator) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortUrlCreator = shortUrlCreator;
    }


    /**
     * Create a new shortened URL.
     *
     * @param request data for URL creation
     * @param user current user
     * @return shortened URL
     */
    @PostMapping
    public ResponseEntity<ShortUrl> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request, @RequestAttribute User user) {
        // Validate the original URL
        if (!shortUrlCreator.isValidUrl(request.getOriginUrl())) {
            return ResponseEntity.badRequest().body(null);
        }

        // Generate unique short URL
        String shortUrl = shortUrlCreator.generateUniqueShortUrl();
        // Create ShortUrl object
        ShortUrl newShortUrl = new ShortUrl();
        newShortUrl.setShortUrl(shortUrl);
        newShortUrl.setOriginUrl(request.getOriginUrl());
        newShortUrl.setDateOfCreating(LocalDateTime.now().toString());
        newShortUrl.setDateOfExpiring(request.getDateOfExpiring());
        newShortUrl.setCountOfTransition(0);
        newShortUrl.setUser(user);

        // Save to repository
        shortUrlRepository.save(newShortUrl);

        return ResponseEntity.status(HttpStatus.CREATED).body(newShortUrl);
    }


    /**
     * Get all short URLs created by the user.
     *
     * @param user current user
     * @return list of URLs
     */
    @GetMapping
    public ResponseEntity<List<ShortUrl>> getAllShortUrls(@RequestAttribute User user) {
        List<ShortUrl> userUrls = shortUrlRepository.findAll()
                .stream()
                .filter(url -> url.getUser().getId() == user.getId())
                .toList();
        return ResponseEntity.ok(userUrls);
    }


    /**
     * Delete a shortened URL by its ID.
     *
     * @param id   URL ID
     * @param user current user
     * @return operation status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable long id, @RequestAttribute User user) {
        Optional<ShortUrl> shortUrl = shortUrlRepository.findById(id);
        if (shortUrl.isEmpty() || shortUrl.get().getUser().getId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        shortUrlRepository.delete(shortUrl.get());
        return ResponseEntity.noContent().build();
    }


    /**
     * Redirect to the original URL and update transition count.
     *
     * @param shortUrl short URL
     * @return redirect to the original URL
     */
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        Optional<ShortUrl> foundUrl = shortUrlRepository.findAll()
                .stream()
                .filter(url -> url.getShortUrl().equals(shortUrl))
                .findFirst();

        if (foundUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ShortUrl url = foundUrl.get();
        url.setCountOfTransition(url.getCountOfTransition() + 1);
        shortUrlRepository.save(url);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", url.getOriginUrl())
                .build();
    }

    /**
     * Retrieves statistics for a specific short URL created by the user.
     *
     * @param shortUrl the short URL for which statistics are requested
     * @param user the current authenticated user making the request
     * @return a {@link ResponseEntity} containing the {@link ShortUrl} object if found,
     *         or a {@link ResponseEntity} with status 404 if the URL does not exist or does not belong to the user
     */

    @GetMapping("/{shortUrl}/stats")
    public ResponseEntity<ShortUrl> getShortUrlStats(@PathVariable String shortUrl , @RequestAttribute User user) {
        Optional<ShortUrl> url = shortUrlRepository.findAll()
                .stream()
                .filter(u->u.getShortUrl().equals(shortUrl) && u.getUser().getId() == user.getId())
                .findFirst();
        if (url.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(url.get());
    }

    /**
     * Finds the original URL based on the provided short URL, ensuring it belongs to the authenticated user.
     *
     * @param originUrl the original URL to search for
     * @param user the current authenticated user making the request
     * @return a {@link ResponseEntity} containing the {@link ShortUrl} object if found,
     *         or a {@link ResponseEntity} with status 404 if the original URL does not exist or does not belong to the user
     */
    @GetMapping("/search")
    public ResponseEntity<ShortUrl> findOriginalUrl(@RequestParam String originUrl, @RequestAttribute User user) {
        Optional<ShortUrl> url = shortUrlRepository.findAll()
                .stream()
                .filter(u->u.getOriginUrl().equals(originUrl) && u.getUser().getId() == user.getId())
                .findFirst();
        if (url.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(url.get());
    }
}



