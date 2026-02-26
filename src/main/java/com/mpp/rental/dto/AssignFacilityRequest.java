package com.mpp.rental.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignFacilityRequest {

    @NotNull(message = "Facility ID is required")
    private Integer facilityId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Max per business is required")
    @Min(value = 1, message = "Max per business must be at least 1")
    private Integer maxPerBusiness;

    @NotNull(message = "Student price is required")
    @DecimalMin(value = "0.00", message = "Student price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Student price must have at most 2 decimal places")
    private BigDecimal studentPrice;

    @NotNull(message = "Non-student price is required")
    @DecimalMin(value = "0.00", message = "Non-student price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Non-student price must have at most 2 decimal places")
    private BigDecimal nonStudentPrice;

    // Optional: for updating existing assignments
    private Integer eventFacilityId;
}
