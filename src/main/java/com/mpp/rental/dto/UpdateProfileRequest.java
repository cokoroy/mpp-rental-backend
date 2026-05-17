package com.mpp.rental.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String userName;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String userPhoneNumber;

    // ── Split address fields ──────────────────────────────────────────────────
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String userAddressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String userAddressLine2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String userCity;

    @Pattern(regexp = "^[0-9]{5}$", message = "Postal code must be 5 digits")
    private String userPostalCode;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String userState;
    // ─────────────────────────────────────────────────────────────────────────

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Pattern(regexp = "^[0-9]{10,20}$", message = "Bank account number must be 10-20 digits")
    private String bankAccNumber;
}