package com.example.ShortenerProject.shortUrl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create a shortened URL")
public class ShortUrlCreateRequest {
//    @NotEmpty(message = "Short URL cannot be empty")
    private String shortUrl;

    @NotEmpty(message = "Origin URL cannot be empty")
    @Schema(description = "Original URL", example = "https://example.com")
    private String originUrl;

    @Schema(description = "Creating date")
    private String dateOfCreating;

    @Schema(description = "Expiration date", example = "2025-12-31T23:59:59")
    private String dateOfExpiring;

    @NotNull(message = "User ID cannot be null")
    @Schema(description = "User ID", example = "1")
    private Long user;
}