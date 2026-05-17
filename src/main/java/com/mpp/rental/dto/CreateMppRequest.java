package com.mpp.rental.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMppRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255)
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String userEmail;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$")
    private String userPhoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Must contain uppercase, lowercase and number")
    private String userPassword;

    // Bank
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^[0-9]{10,20}$")
    private String bankAccNumber;
}