package com.example.ShortenerProject.user.dto.response;

import lombok.Builder;

@Builder
public record UserResponse(long id, String username) {
}
