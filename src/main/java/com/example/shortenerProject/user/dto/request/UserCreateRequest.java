package com.example.shortenerProject.user.dto.request;

import lombok.Builder;

@Builder
public record UserCreateRequest(String username,
                                String password) {
}
