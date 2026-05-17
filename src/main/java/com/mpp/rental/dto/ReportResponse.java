package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic report wrapper — summary type S, row type R.
 * Replaces the old ReportResponse so all 5 reports share one wrapper class.
 *
 * Examples:
 *   ReportResponse<FacilityRentalSummary, FacilityRentalRow>
 *   ReportResponse<RevenueSummary, RevenueRow>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse<S, R> {
    private S       summary;
    private List<R> rows;
}