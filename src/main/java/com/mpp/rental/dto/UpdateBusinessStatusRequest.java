package com.mpp.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBusinessStatusRequest {

    @NotBlank(message = "Business status is required")
    @Pattern(regexp = "ACTIVE|BLOCKED|INACTIVE", 
             message = "Business status must be ACTIVE, BLOCKED, or INACTIVE")
    private String businessStatus;

    /**
     * New status for the business
     * Values: ACTIVE, BLOCKED
     */

    /**
     * Optional reason for status change
     * Especially useful when blocking a business
     */
    private String reason;
}
