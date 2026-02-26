package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilitySearchFilterRequest {

    private String searchQuery;     // Search by facility name
    private String facilityType;    // Filter by type (or "all")
    private String facilitySize;    // Filter by size (or "all")
    private String facilityStatus;  // Filter by status (or "all")
}