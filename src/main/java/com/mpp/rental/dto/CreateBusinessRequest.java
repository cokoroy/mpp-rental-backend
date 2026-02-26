package com.mpp.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(min = 3, max = 255, message = "Business name must be between 3 and 255 characters")
    private String businessName;

    // SSM number is conditionally required (validated in service layer)
    @Size(max = 255, message = "SSM number must not exceed 255 characters")
    private String ssmNumber;

    @NotBlank(message = "Business category is required")
    @Size(max = 255, message = "Business category must not exceed 255 characters")
    private String businessCategory;

    @Size(max = 255, message = "Business description must not exceed 255 characters")
    private String businessDesc;
}
