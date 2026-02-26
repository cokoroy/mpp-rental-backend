package com.mpp.rental.controller;

import com.mpp.rental.dto.ApiResponse;
import com.mpp.rental.dto.LoginRequest;
import com.mpp.rental.dto.LoginResponse;
import com.mpp.rental.dto.RegisterRequest;
import com.mpp.rental.dto.UserProfileResponse;
import com.mpp.rental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - Handles authentication endpoints
 * Base URL: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Register new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserProfileResponse userProfile = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userProfile));
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse loginResponse = userService.loginUser(request);

        return ResponseEntity
                .ok(ApiResponse.success("Login successful", loginResponse));
    }

    /**
     * Test endpoint - check if server is running
     * GET /api/auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity
                .ok(ApiResponse.success("Server is running!", "MPP Rental Management System API"));
    }
}