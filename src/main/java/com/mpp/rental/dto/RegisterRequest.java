package com.mpp.rental.dto;

import com.mpp.rental.model.User.UserCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String userEmail;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String userPhoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String userPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    @NotNull(message = "User category is required")
    // Only STUDENT and NON_STUDENT allowed — MPP/SUPER_ADMIN blocked in UserService
    private UserCategory userCategory;

    // ── Split address fields ──────────────────────────────────────────────────
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String userAddressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String userAddressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String userCity;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[0-9]{5}$", message = "Postal code must be 5 digits")
    private String userPostalCode;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String userState;
    // ─────────────────────────────────────────────────────────────────────────

    // Bank Account Information
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Bank account number must be 10-20 digits")
    private String bankAccNumber;
}