package com.example.shortenerProject.user.dto.response;

import lombok.Builder;

@Builder
public record RegistrationResponse(UserResponse userResponse, String token, String message) {
}
