package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class FacilityRentalSummary {
    private int        totalApplications;
    private int        totalApproved;
    private int        totalRejected;
    private int        totalPending;
    private int        totalCancelled;
    private int        totalBusinesses;      // distinct business count
    private int        totalPaid;            // count of PAID payments
    private BigDecimal totalRevenue;         // sum of PAID amounts
    private BigDecimal totalUnpaid;          // sum of UNPAID amounts
    private int        collectionRate;       // totalPaid / totalApproved * 100
}