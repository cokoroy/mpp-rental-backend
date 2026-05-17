package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class RevenueSummary {
    private BigDecimal totalBilled;           // all payment records (PAID + UNPAID + FAILED)
    private BigDecimal totalCollected;        // PAID only
    private BigDecimal totalOutstanding;      // UNPAID only
    private BigDecimal totalFailed;           // FAILED only
    private int        collectionRate;        // PAID / (PAID + UNPAID) * 100
    private BigDecimal avgPaymentPerBusiness; // totalCollected / distinct paid businesses
    private int        totalPaymentRecords;
    private int        totalPaidCount;
    private int        totalUnpaidCount;
    private int        totalFailedCount;
}