package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class FacilityUtilisationSummary {
    private int    totalFacilities;
    private int    overallFillRate;          // total filled / total slots * 100
    private String mostDemandedFacility;    // name with most applications
    private int    mostDemandedCount;
    private String highestFillRateFacility; // name with highest fill %
    private String lowestFillRateFacility;  // name with lowest fill % (underutilised)
}