package com.mpp.rental.dto;

import com.mpp.rental.model.User.UserCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegisterRequest DTO - Used for user registration
 * Contains validation annotations to ensure data quality
 */
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
    private UserCategory userCategory;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String userAddress;

    // Bank Account Information
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Bank account number must be 10-20 digits")
    private String bankAccNumber;
}