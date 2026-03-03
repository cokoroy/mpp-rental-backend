package com.mpp.rental.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryResponse {

    private int        totalApplications;
    private int        totalApproved;
    private int        totalRejected;
    private int        totalPending;
    private int        totalCancelled;
    private int        totalBusinesses;   // distinct business count
    private int        totalPaid;         // count of PAID payments (used for collection rate)
    private BigDecimal totalRevenue;      // sum of PAID payment amounts
    private BigDecimal totalUnpaid;       // sum of UNPAID payment amounts
}