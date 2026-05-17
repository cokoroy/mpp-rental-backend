package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class BusinessActivitySummary {
    private int    totalBusinesses;
    private int    totalApplications;
    private int    overallApprovalRate;    // totalApproved / totalApplications * 100
    private String mostActiveOwner;        // business name with most applications
    private int    mostActiveOwnerCount;
    private String highestCancellationOwner; // business name with highest cancellation rate
}