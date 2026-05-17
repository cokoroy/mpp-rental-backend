package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.service.OtpService;
import com.mpp.rental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final OtpService  otpService;

    // ==================== REGISTER ====================

    /**
     * POST /api/auth/register
     * MPP: registers directly (no OTP).
     * STUDENT / NON_STUDENT: requires OTP to have been verified before calling this.
     * The OTP is verified inside registerUser() — if not verified, it throws BadRequestException.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            UserProfileResponse userProfile = userService.registerUser(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", userProfile));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== OTP ====================

    /**
     * POST /api/auth/send-otp
     * Generates and emails a 6-digit OTP to the given address.
     * Called when user clicks "Send Verification Code" on the register form.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(
            @Valid @RequestBody OtpRequest request) {
        try {
            // Block if email already registered
            if (userService.emailExists(request.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Email is already registered."));
            }
            otpService.sendOtp(request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Verification code sent to " + request.getEmail(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/verify-otp
     * Verifies the OTP entered by the user.
     * On success, marks the email as verified in the OTP store so register can proceed.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        try {
            boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());
            if (!valid) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid or expired verification code."));
            }
            // Mark as verified so registerUser() knows OTP was completed
            otpService.markVerified(request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Email verified successfully.", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== LOGIN ====================

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = userService.loginUser(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(
                ApiResponse.success("Server is running!", "MPP Rental Management System API"));
    }
}