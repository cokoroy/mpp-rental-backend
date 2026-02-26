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

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String userAddress;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Pattern(regexp = "^[0-9]{10,20}$", message = "Bank account number must be 10-20 digits")
    private String bankAccNumber;
}