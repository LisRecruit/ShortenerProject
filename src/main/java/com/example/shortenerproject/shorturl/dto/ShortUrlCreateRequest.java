package com.example.shortenerproject.shorturl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create a shortened URL")
public class ShortUrlCreateRequest {


    @NotEmpty(message = "Origin URL cannot be empty")
    @Schema(description = "Original URL", example = "https://example.com")
    private String originUrl;



//    @NotNull(message = "User ID cannot be null")
//    @Schema(description = "User ID", example = "1")
//    private Long user;
}