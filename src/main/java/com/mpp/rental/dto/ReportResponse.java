package com.mpp.rental.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Top-level wrapper returned by GET /api/mpp/reports
 * Contains summary metrics + row-level data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private ReportSummaryResponse      summary;
    private List<ReportRowResponse>    rows;
}