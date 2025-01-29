package com.example.shortenerproject.shorturl;

import com.example.shortenerproject.exception.dto.ErrorResponse;

import com.example.shortenerproject.security.JwtUtil;
import com.example.shortenerproject.shorturl.dto.request.ShortUrlCreateRequest;
import com.example.shortenerproject.shorturl.dto.response.ShortUrlResponse;
import com.example.shortenerproject.shorturl.dto.response.ShortUrlStatsResponse;
import com.example.shortenerproject.user.User;
import com.example.shortenerproject.user.UserRepository;
import com.example.shortenerproject.user.UserService;
import com.example.shortenerproject.utils.Validator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/short-urls")
@Tag(name = "Short URL API", description = "API for managing shortened URLs")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final Validator validator;

    @Autowired
    public ShortUrlController(ShortUrlService shortUrlService, JwtUtil jwtUtil, UserRepository userRepository, UserService userService, Validator validator) {
        this.shortUrlService = shortUrlService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @Operation(
            summary = "Get all shortened URLs",
            description = "Retrieve a list of all shortened URLs created by the user or all users, depending on authorization.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all shortened URLs",
                            content = @Content(schema = @Schema(implementation = ShortUrlResponse[].class))),
                    @ApiResponse(responseCode = "404", description = "No shortened URLs found")
            }
    )
    @GetMapping
    public ResponseEntity<?> getAllShortUrls() {
        List<ShortUrlResponse> response = shortUrlService.findAllShortUrls();
        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("404","No shortened URLs found"));
        } else {
            return ResponseEntity.ok(response);
        }
    }



    @Operation(
            summary = "Create a new shortened URL",
            description = "Allows registered users to generate a shortened URL for a given original URL.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload for creating a shortened URL",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ShortUrlCreateRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Shortened URL created successfully",
                            content = @Content(schema = @Schema(implementation = ShortUrlResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data")
            }
    )
    @PostMapping
    public ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody ShortUrlCreateRequest request,
                                                           @RequestHeader("Authorization") String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        ShortUrlResponse response = shortUrlService.createShortUrl(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(
            summary = "Get all shortened URLs by the user",
            description = "Retrieve a list of all shortened URLs created by the authenticated user.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT token for authentication", required = true, in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of shortened URLs",
                            content = @Content(schema = @Schema(implementation = ShortUrlResponse[].class)))
            }
    )
    @GetMapping("/my-urls")
    public ResponseEntity<List<ShortUrlResponse>> getAllShortUrlsByUser(@RequestHeader("Authorization") String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<ShortUrlResponse> response = shortUrlService.findAllShortUrlsByUser(user);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a shortened URL",
            description = "Delete a shortened URL by its ID. Only the owner can delete their URLs.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the shortened URL", required = true),
                    @Parameter(name = "Authorization", description = "JWT token for authentication", required = true, in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "URL deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not own the URL"),
                    @ApiResponse(responseCode = "404", description = "URL not found")
            }
    )
    @DeleteMapping("/my-urls/{id}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable long id, @RequestHeader("Authorization") String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Optional<ShortUrlResponse> shortUrl = shortUrlService.findByIdAndUser(id, user);
        if (shortUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        shortUrlService.deleteShortUrl(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Redirect to the original URL",
            description = "Redirects the user to the original URL associated with the given short URL.",
            parameters = {
                    @Parameter(name = "shortUrl", description = "The shortened URL to be redirected", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "302", description = "Redirecting to the original URL"),
                    @ApiResponse(responseCode = "400", description = "Shortened URL is expired"),
                    @ApiResponse(responseCode = "404", description = "Shortened URL not found")
            }
    )
    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> redirect(@PathVariable String shortUrl) {
        Optional<ShortUrl> foundUrl = shortUrlService.findAndRedirect(shortUrl);
        if (foundUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else if(!validator.isDateValid(foundUrl.get())) {
            return ResponseEntity.status(400).body("Shortened URL is expired");
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
                    @Parameter(name = "Authorization", description = "JWT token for authentication", required = true, in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statistics for the shortened URL",
                            content = @Content(schema = @Schema(implementation = ShortUrlStatsResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Shortened URL not found")
            }
    )
    @GetMapping("/my-urls/{shortUrl}/stats")
    public ResponseEntity<ShortUrlStatsResponse> getShortUrlStats(@PathVariable String shortUrl, @RequestHeader("Authorization") String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Optional<ShortUrlStatsResponse> stats = shortUrlService.getShortUrlStats(shortUrl, user);

        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats.get());
    }

    @Operation(
            summary = "Find the original URL",
            description = "Retrieve the original URL based on the given shortened URL. Requires authentication.",
            parameters = {
                    @Parameter(name = "shortUrl", description = "The shortened URL to look up", required = true),
                    @Parameter(name = "Authorization", description = "JWT token for authentication", required = true, in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved original URL",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                    @ApiResponse(responseCode = "404", description = "Shortened URL not found")
            }
    )
    @GetMapping("/my-urls/find/{shortUrl}")
    public ResponseEntity<String> findOriginalUrl(@PathVariable String shortUrl, @RequestHeader("Authorization") String token) {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", Long.class));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Optional<String> originUrl = shortUrlService.findOriginalUrl(shortUrl, user);
        if (originUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(originUrl.get());
    }
}



