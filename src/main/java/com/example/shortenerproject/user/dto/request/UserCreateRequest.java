package com.example.shortenerproject.user.dto.request;

import lombok.Builder;

@Builder
public record UserCreateRequest(String username,
                                String password) {
}
