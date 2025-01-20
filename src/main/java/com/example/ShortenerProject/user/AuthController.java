package com.example.ShortenerProject.user;

import com.example.ShortenerProject.security.JwtUtil;
import com.example.ShortenerProject.user.dto.request.LoginRequest;
import com.example.ShortenerProject.user.dto.request.UserCreateRequest;
import com.example.ShortenerProject.user.dto.response.AuthResponse;
import com.example.ShortenerProject.user.dto.response.RegistrationResponse;
import com.example.ShortenerProject.user.dto.response.UserResponse;
import com.example.ShortenerProject.utils.Validator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Tag(name = "Authentication", description = "API for user authentication and registration")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(
            summary = "User authentication",
            description = "Accepts a username and password, verifies them, and returns a JWT token",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successful authentication, returns JWT token",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Authentication failed due to incorrect username or password"
                    )
            }
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/registration")
    @Operation(
            summary = "New user registration",
            description = "Registers a new user with the provided data and returns a JWT token",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "User successfully registered and authenticated",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = RegistrationResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Validation failed for input data"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Server error occurred during registration"
                    )
            }
    )
    public ResponseEntity<?> registration(@RequestBody UserCreateRequest request) {
        if (!Validator.isValidPassword(request.password())) {
            return ResponseEntity.status(400).body("Password must contain at least 8 characters, including digits, uppercase and lowercase letters.");
        }
        try {
            String creationMessage = userService.createUser(request);

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
            String token = jwtUtil.generateToken(userDetails);

            UserResponse userResponse = UserResponse.builder()
                    .username(request.username())
                    .build();

            RegistrationResponse response = RegistrationResponse.builder()
                    .token(token)
                    .userResponse(userResponse)
                    .message(creationMessage)
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
