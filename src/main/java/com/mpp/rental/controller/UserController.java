package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.model.User.UserCategory;
import com.mpp.rental.model.User.UserStatus;
import com.mpp.rental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController - Handles user profile management
 * Base URL: /api/users
 * All endpoints require authentication
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==================== EXISTING ENDPOINTS ====================

    /**
     * Get current logged-in user's profile
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile() {
        // Get email of currently authenticated user from Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserProfileResponse profile = userService.getUserProfileByEmail(email);

        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    /**
     * Get user profile by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);

        return ResponseEntity
                .ok(ApiResponse.success("User found", profile));
    }

    /**
     * Update current user's profile
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        // Get current user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Get user ID from email
        UserProfileResponse currentProfile = userService.getUserProfileByEmail(email);

        // Update profile
        UserProfileResponse updatedProfile = userService.updateUserProfile(
                currentProfile.getUserId(),
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    /**
     * Change password
     * PUT /api/users/change-password
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        // Get current user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Change password
        userService.changePassword(email, request);

        return ResponseEntity
                .ok(ApiResponse.success("Password changed successfully", null));
    }

    // ==================== NEW MPP USER MANAGEMENT ENDPOINTS ====================

    /**
     * Get all users (MPP only)
     * GET /api/users/mpp/all
     */
    @GetMapping("/mpp/all")
    @PreAuthorize("hasRole('MPP')") // Only MPP can access this endpoint
    public ResponseEntity<ApiResponse<List<UserManagementResponse>>> getAllUsers() {
        List<UserManagementResponse> users = userService.getNonAdminUsers();

        return ResponseEntity
                .ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Search and filter users (MPP only)
     * GET /api/users/mpp/search
     * Query params: searchQuery, category, status
     */
    @GetMapping("/mpp/search")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<UserManagementResponse>>> searchUsers(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) UserCategory category,
            @RequestParam(required = false) UserStatus status) {

        List<UserManagementResponse> users = userService.searchUsers(searchQuery, category, status)
                .stream()
                .filter(u -> u.getUserCategory() != com.mpp.rental.model.User.UserCategory.MPP
                        && u.getUserCategory() != com.mpp.rental.model.User.UserCategory.SUPER_ADMIN)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity
                .ok(ApiResponse.success("Search completed successfully", users));
    }

    /**
     * Get user details including password (MPP only)
     * GET /api/users/mpp/details/{userId}
     */
    @GetMapping("/mpp/details/{userId}")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getUserDetails(@PathVariable Long userId) {
        UserDetailsResponse userDetails = userService.getUserDetailsById(userId);

        return ResponseEntity
                .ok(ApiResponse.success("User details retrieved successfully", userDetails));
    }

    /**
     * Update user by MPP (MPP only)
     * PUT /api/users/mpp/update/{userId}
     */
    @PutMapping("/mpp/update/{userId}")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> updateUserByMPP(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserByMPPRequest request) {

        UserDetailsResponse updatedUser = userService.updateUserByMPP(userId, request);

        return ResponseEntity
                .ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    /**
     * Toggle user status - Block/Activate (MPP only)
     * PUT /api/users/mpp/toggle-status/{userId}
     */
    @PutMapping("/mpp/toggle-status/{userId}")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> toggleUserStatus(@PathVariable Long userId) {
        UserDetailsResponse updatedUser = userService.toggleUserStatus(userId);

        String message = updatedUser.getUserStatus() == UserStatus.ACTIVE
                ? "User activated successfully"
                : "User blocked successfully";

        return ResponseEntity
                .ok(ApiResponse.success(message, updatedUser));
    }

    // ==================== SUPER ADMIN ENDPOINTS ====================

    /**
     * Get ALL users including MPP and SUPER_ADMIN (Super Admin only)
     * GET /api/users/superadmin/all
     */
    @GetMapping("/superadmin/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserManagementResponse>>> getAllUsersForSuperAdmin() {
        try {
            return ResponseEntity.ok(ApiResponse.success("All users retrieved", userService.getAllUsers()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all MPP users (Super Admin only)
     * GET /api/users/superadmin/mpp
     */
    @GetMapping("/superadmin/mpp")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserManagementResponse>>> getMppUsers() {
        try {
            return ResponseEntity.ok(ApiResponse.success("MPP users retrieved", userService.getMppUsers()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create new MPP account (Super Admin only)
     * POST /api/users/superadmin/mpp
     */
    @PostMapping("/superadmin/mpp")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createMppUser(
            @Valid @RequestBody com.mpp.rental.dto.CreateMppRequest request) {
        try {
            UserProfileResponse created = userService.createMppUser(request);
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                    .body(ApiResponse.success("MPP account created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Toggle MPP user status (Super Admin only)
     * PUT /api/users/superadmin/mpp/toggle-status/{userId}
     */
    @PutMapping("/superadmin/mpp/toggle-status/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> toggleMppStatus(@PathVariable Long userId) {
        try {
            UserDetailsResponse updated = userService.toggleUserStatus(userId);
            String message = updated.getUserStatus() == com.mpp.rental.model.User.UserStatus.ACTIVE
                    ? "MPP account activated" : "MPP account blocked";
            return ResponseEntity.ok(ApiResponse.success(message, updated));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update MPP account (Super Admin only)
     * PUT /api/users/superadmin/mpp/{userId}
     */
    @PutMapping("/superadmin/mpp/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> updateMppUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserByMPPRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success("MPP account updated", userService.updateUserByMPP(userId, request)));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}