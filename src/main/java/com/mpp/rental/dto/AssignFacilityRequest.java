package com.mpp.rental.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignFacilityRequest {

    private Integer eventFacilityId; // For updates

    @NotNull(message = "Facility ID is required")
    private Integer facilityId;

    // NEW: Allocation mode flag
    @NotNull(message = "Allocation mode is required")
    private Boolean isAllocatedByCategory = false;

    // Used when isAllocatedByCategory = true
    @Min(value = 0, message = "Student quantity must be at least 0")
    private Integer quantityStudent = 0;

    @Min(value = 0, message = "Non-student quantity must be at least 0")
    private Integer quantityNonStudent = 0;

    // Used when isAllocatedByCategory = false (open to all)
    @Min(value = 1, message = "Total quantity must be at least 1")
    private Integer totalQuantity;

    @NotNull(message = "Max per business is required")
    @Min(value = 1, message = "Max per business must be at least 1")
    private Integer maxPerBusiness;

    @NotNull(message = "Student price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Student price must be at least 0")
    private BigDecimal studentPrice;

    @NotNull(message = "Non-student price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Non-student price must be at least 0")
    private BigDecimal nonStudentPrice;
}