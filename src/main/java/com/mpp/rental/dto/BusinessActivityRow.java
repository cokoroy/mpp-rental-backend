package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class BusinessActivityRow {
    private Long       businessId;
    private String     businessName;
    private String     ownerName;
    private String     ownerCategory;        // STUDENT | NON_STUDENT
    private int        totalApplied;
    private int        totalApproved;
    private int        totalRejected;
    private int        totalCancelled;
    private int        totalPaid;
    private int        approvalRate;          // approved / totalApplied * 100
    private int        cancellationRate;      // cancelled / totalApplied * 100
    private int        paymentRate;           // paid / approved * 100
    private BigDecimal totalRevenuePaid;      // sum of PAID payments for this business
}