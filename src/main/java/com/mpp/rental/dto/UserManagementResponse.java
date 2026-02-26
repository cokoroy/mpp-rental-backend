package com.mpp.rental.dto;

import com.mpp.rental.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserManagementResponse - DTO for user list in MPP User Management
 * Contains user info + list of their businesses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {

    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;
    private User.UserCategory userCategory;
    private User.UserStatus userStatus;
    private String userAddress;
    private LocalDateTime userRegisteredAt;
    private LocalDateTime userLastLogin;

    // Bank Account Info
    private String bankName;
    private String bankAccNumber;

    // List of businesses owned by this user
    private List<UserBusinessSummary> businesses;

    /**
     * Nested class for business summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBusinessSummary {
        private Long businessId;
        private String businessName;
        private String businessCategory;
        private String businessStatus;
    }
}