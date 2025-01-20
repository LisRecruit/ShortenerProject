package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlStatsResponse;
import com.example.ShortenerProject.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/short-urls")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;


    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;

    }


    /**
     * Create a new shortened URL.
     *
     * @param request data for URL creation
     * @return shortened URL
     */

    @PostMapping
    public ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody ShortUrlCreateRequest request) {
        ShortUrlResponse response = shortUrlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


    /**
     * Get all short URLs created by the user.
     *
     * @param user current user
     * @return list of URLs
     */

    @GetMapping
    public ResponseEntity<List<ShortUrlResponse>> getAllShortUrlsByUser(@RequestAttribute User user) {
        List<ShortUrlResponse> response = shortUrlService.findAllShortUrlsByUser(user);
        return ResponseEntity.ok(response);
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
        Optional<ShortUrlResponse> shortUrl = shortUrlService.findByIdAndUser(id, user);
        if (shortUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        shortUrlService.deleteShortUrl(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Redirect to the original URL and update transition count.
     *
     * @param shortUrl short URL
     * @return redirect to the original URL
     */

    @GetMapping("/{shortUrl}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        Optional<ShortUrl> foundUrl = shortUrlService.findAndRedirect(shortUrl);

        if (foundUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", foundUrl.get().getOriginUrl())
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
    public ResponseEntity<ShortUrlStatsResponse> getShortUrlStats(@PathVariable String shortUrl , @RequestAttribute User user) {
        Optional<ShortUrlStatsResponse> stats = shortUrlService.getShortUrlStats(shortUrl, user);

        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats.get());
    }

    /**
     * Finds the original URL based on the provided short URL, ensuring it belongs to the authenticated user.
     *
     * @param user the current authenticated user making the request
     * @return a {@link ResponseEntity} containing the {@link ShortUrl} object if found,
     *         or a {@link ResponseEntity} with status 404 if the original URL does not exist or does not belong to the user
     */

    @GetMapping("/search")
    public ResponseEntity<String> findOriginalUrl(@RequestParam String shortUrl, @RequestAttribute User user) {
        Optional<String> originUrl = shortUrlService.findOriginalUrl(shortUrl, user);
        if (originUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(originUrl.get());
    }
}



