package com.example.ShortenerProject.shortUrl.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateShortUrlRequest {

    @NotBlank(message = "Origin URL cannot be blank")
    @Pattern(
            regexp = "^(http://|https://).+",
            message = "Origin URL must start with http:// or https://"
    )
    private String originUrl; // Оригинальный URL

    @NotBlank(message = "Expiration date cannot be blank")
    private String dateOfExpiring; // Дата истечения срока действия (в формате ISO 8601)
}
