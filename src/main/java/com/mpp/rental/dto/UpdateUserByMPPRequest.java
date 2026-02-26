package com.mpp.rental.dto;

import com.mpp.rental.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateUserByMPPRequest - DTO for MPP to update user information
 * MPP can update ALL fields including role and password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserByMPPRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String userEmail;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String userPhoneNumber;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String userAddress;

    @NotNull(message = "User category is required")
    private User.UserCategory userCategory; // STUDENT or NON_STUDENT

    // Bank Account Information
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Size(max = 50, message = "Bank account number cannot exceed 50 characters")
    private String bankAccNumber;

    // Password (optional - only update if provided)
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String userPassword; // If null or empty, don't update password

    // Role (MPP can change user role)
    // Note: In your current User model, there's no separate "role" field
    // The role is determined by userCategory (MPP, STUDENT, NON_STUDENT)
    // If you want to allow changing between BUSINESS_OWNER and MPP roles,
    // you might need to add a separate role field or use userCategory
}