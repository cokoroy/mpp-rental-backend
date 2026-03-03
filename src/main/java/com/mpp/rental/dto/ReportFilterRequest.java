package com.mpp.rental.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilterRequest {

    private Integer   eventId;
    private Integer   facilityId;

    /** STUDENT or NON_STUDENT — null means all */
    private String    ownerCategory;

    /** PENDING, APPROVED, REJECTED, CANCELLED — null means all */
    private String    applicationStatus;

    /** PAID, UNPAID, FAILED — null means all */
    private String    paymentStatus;

    private LocalDate startDate;
    private LocalDate endDate;
}