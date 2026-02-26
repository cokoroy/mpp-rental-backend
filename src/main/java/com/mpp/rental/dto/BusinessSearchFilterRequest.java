package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for MPP Business Search and Filter Request
 * All fields are optional for flexible searching
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessSearchFilterRequest {

    /**
     * Search query - searches across:
     * - Business name
     * - SSM number
     * - Owner name
     */
    private String searchQuery;

    /**
     * Filter by business category
     * Examples: Food, Clothing, Services, Arts & Crafts, Technology, Other
     */
    private String businessCategory;

    /**
     * Filter by business status
     * Values: ACTIVE, BLOCKED
     */
    private String businessStatus;

    /**
     * Filter by owner category
     * Values: STUDENT, NON_STUDENT
     * (MPP businesses are excluded from business management)
     */
    private String ownerCategory;

    /**
     * Filter by registration date range
     */
    private LocalDate startDate;
    private LocalDate endDate;
}