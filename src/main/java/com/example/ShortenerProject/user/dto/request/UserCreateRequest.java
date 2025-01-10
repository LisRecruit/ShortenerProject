package com.example.ShortenerProject.user.dto.request;

import lombok.Builder;

@Builder
public record UserCreateRequest(String username,
                                String password) {
}
