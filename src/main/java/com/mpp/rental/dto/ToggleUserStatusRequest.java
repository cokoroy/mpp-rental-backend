package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ToggleUserStatusRequest - DTO for toggling user status
 * MPP can block or activate users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleUserStatusRequest {

    private String reason; // Optional: Reason for blocking/activating (for notifications)
}