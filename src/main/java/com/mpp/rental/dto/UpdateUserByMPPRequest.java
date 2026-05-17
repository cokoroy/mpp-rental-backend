package com.mpp.rental.dto;

import com.mpp.rental.model.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // ── Split address fields ──────────────────────────────────────────────────
    @Size(max = 255, message = "Address line 1 cannot exceed 255 characters")
    private String userAddressLine1;

    @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
    private String userAddressLine2;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String userCity;

    @Pattern(regexp = "^[0-9]{5}$", message = "Postal code must be 5 digits")
    private String userPostalCode;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String userState;
    // ─────────────────────────────────────────────────────────────────────────

    @NotNull(message = "User category is required")
    private User.UserCategory userCategory;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Size(max = 50, message = "Bank account number cannot exceed 50 characters")
    private String bankAccNumber;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String userPassword;
}