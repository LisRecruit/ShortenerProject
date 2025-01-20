package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlStatsResponse;
import com.example.ShortenerProject.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/short-urls")
@Tag(name = "Short URL API", description = "API for managing shortened URLs")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @Operation(
            summary = "Create a new shortened URL",
            description = "Allows registered users to create a new shortened URL for a given original URL.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details for creating a shortened URL",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ShortUrlCreateRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Shortened URL created successfully",
                            content = @Content(schema = @Schema(implementation = ShortUrlResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    public ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody ShortUrlCreateRequest request) {
        ShortUrlResponse response = shortUrlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(
            summary = "Get all shortened URLs by the user",
            description = "Retrieve a list of all shortened URLs created by the authenticated user.",
            parameters = {
                    @Parameter(name = "user", hidden = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of shortened URLs",
                            content = @Content(schema = @Schema(implementation = ShortUrlResponse[].class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<ShortUrlResponse>> getAllShortUrlsByUser(@RequestAttribute User user) {
        List<ShortUrlResponse> response = shortUrlService.findAllShortUrlsByUser(user);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a shortened URL",
            description = "Delete a shortened URL by its ID. Only the owner can delete their URLs.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the shortened URL", required = true),
                    @Parameter(name = "user", hidden = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "URL deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not own the URL"),
                    @ApiResponse(responseCode = "404", description = "URL not found")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable long id, @RequestAttribute User user) {
        Optional<ShortUrlResponse> shortUrl = shortUrlService.findByIdAndUser(id, user);
        if (shortUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        shortUrlService.deleteShortUrl(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Redirect to the original URL",
            description = "Redirect to the original URL associated with the given short URL.",
            parameters = {
                    @Parameter(name = "shortUrl", description = "Shortened URL", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "302", description = "Redirect to the original URL",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "Shortened URL not found")
            }
    )
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

    @Operation(
            summary = "Get statistics for a specific short URL",
            description = "Retrieve statistics such as usage count for a specific shortened URL owned by the user.",
            parameters = {
                    @Parameter(name = "shortUrl", description = "Shortened URL", required = true),
                    @Parameter(name = "user", hidden = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statistics for the shortened URL",
                            content = @Content(schema = @Schema(implementation = ShortUrlStatsResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Shortened URL not found")
            }
    )
    @GetMapping("/{shortUrl}/stats")
    public ResponseEntity<ShortUrlStatsResponse> getShortUrlStats(@PathVariable String shortUrl, @RequestAttribute User user) {
        Optional<ShortUrlStatsResponse> stats = shortUrlService.getShortUrlStats(shortUrl, user);

        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats.get());
    }

    @Operation(
            summary = "Find the original URL",
            description = "Retrieve the original URL based on the given shortened URL.",
            parameters = {
                    @Parameter(name = "shortUrl", description = "Shortened URL", required = true),
                    @Parameter(name = "user", hidden = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Original URL found",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Shortened URL not found")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<String> findOriginalUrl(@RequestParam String shortUrl, @RequestAttribute User user) {
        Optional<String> originUrl = shortUrlService.findOriginalUrl(shortUrl, user);
        if (originUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(originUrl.get());
    }
}



