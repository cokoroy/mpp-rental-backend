package com.mpp.rental.dto;

import com.mpp.rental.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserDetailsResponse - Complete user details for MPP viewing
 * Includes password (viewable by MPP only)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {

    // Personal Information
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;
    private String userAddress;
    private String userPassword; // Plain text password for MPP to view (will be shown as is)

    // User Category & Role
    private User.UserCategory userCategory;
    private User.UserStatus userStatus;

    // Bank Account Information
    private String bankName;
    private String bankAccNumber;

    // Account Information
    private LocalDateTime userRegisteredAt;
    private LocalDateTime userLastLogin;
    private Boolean emailVerified;

    // List of registered businesses
    private List<BusinessDetails> businesses;

    /**
     * Nested class for business details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessDetails {
        private Long businessId;
        private String businessName;
        private String businessCategory;
        private String businessStatus;
        private String ssmNumber;
        private LocalDateTime businessRegisteredAt;
    }
}