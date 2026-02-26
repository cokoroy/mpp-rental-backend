package com.mpp.rental.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFacilityRequest {

    @NotBlank(message = "Facility name is required")
    @Size(min = 3, max = 100, message = "Facility name must be between 3 and 100 characters")
    private String facilityName;

    @NotBlank(message = "Facility size is required")
    @Size(min = 2, max = 50, message = "Facility size must be between 2 and 50 characters")
    private String facilitySize;

    @NotBlank(message = "Facility type is required")
    @Size(min = 2, max = 50, message = "Facility type must be between 2 and 50 characters")
    private String facilityType;

    @NotBlank(message = "Facility description is required")
    @Size(min = 1, max = 500, message = "Facility description must be between 0 and 500 characters")
    private String facilityDesc;

    @NotBlank(message = "Usage information is required")
    @Size(min = 10, max = 1000, message = "Usage information must be between 10 and 1000 characters")
    private String usage;

    @Size(max = 500, message = "Remark must not exceed 500 characters")
    private String remark;

    @NotNull(message = "Student price is required")
    @DecimalMin(value = "0.00", message = "Student price must be greater than -1")
    @Digits(integer = 10, fraction = 2, message = "Student price must have at most 2 decimal places")
    private BigDecimal facilityBaseStudentPrice;

    @NotNull(message = "Non-student price is required")
    @DecimalMin(value = "0.00", message = "Non-student price must be greater than -1")
    @Digits(integer = 10, fraction = 2, message = "Non-student price must have at most 2 decimal places")
    private BigDecimal facilityBaseNonstudentPrice;

    @NotBlank(message = "Facility status is required")
    @Pattern(regexp = "active|inactive", message = "Status must be either 'active' or 'inactive'")
    private String facilityStatus;

    // Image will be handled separately as MultipartFile in controller
}