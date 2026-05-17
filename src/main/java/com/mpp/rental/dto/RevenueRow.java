package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class RevenueRow {
    private Integer       applicationId;
    private String        eventName;
    private String        businessName;
    private String        ownerName;
    private String        ownerCategory;       // STUDENT | NON_STUDENT
    private BigDecimal    amountBilled;
    private String        paymentStatus;       // PAID | UNPAID | FAILED
    private LocalDateTime paymentCreatedAt;    // when payment record was created (= approval date)
}