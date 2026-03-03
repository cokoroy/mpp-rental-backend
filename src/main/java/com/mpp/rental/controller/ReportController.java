package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mpp/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    // ==================== GENERATE REPORT ====================

    /**
     * GET /api/mpp/reports
     * All params optional — omitting returns all records.
     *
     * @param eventId           filter by event ID
     * @param facilityId        filter by facility ID
     * @param ownerCategory     STUDENT | NON_STUDENT
     * @param applicationStatus PENDING | APPROVED | REJECTED | CANCELLED
     * @param paymentStatus     PAID | UNPAID | FAILED
     * @param startDate         ISO date (yyyy-MM-dd) — applied on applicationCreatedAt
     * @param endDate           ISO date (yyyy-MM-dd) — applied on applicationCreatedAt
     */
    @GetMapping
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @RequestParam(required = false) Integer   eventId,
            @RequestParam(required = false) Integer   facilityId,
            @RequestParam(required = false) String    ownerCategory,
            @RequestParam(required = false) String    applicationStatus,
            @RequestParam(required = false) String    paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            ReportFilterRequest req = new ReportFilterRequest(
                    eventId, facilityId,
                    ownerCategory, applicationStatus, paymentStatus,
                    startDate, endDate
            );
            ReportResponse data = reportService.generateReport(req);
            return ResponseEntity.ok(ApiResponse.success("Report generated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== DROPDOWN DATA ====================

    /**
     * GET /api/mpp/reports/events
     * Returns all non-deleted events for the Event filter dropdown.
     */
    @GetMapping("/events")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsForFilter() {
        try {
            List<EventResponse> events = reportService.getEventsForFilter();
            return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/mpp/reports/facilities
     * Returns all non-deleted facilities for the Facility filter dropdown.
     */
    @GetMapping("/facilities")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getFacilitiesForFilter() {
        try {
            List<FacilityResponse> facilities = reportService.getFacilitiesForFilter();
            return ResponseEntity.ok(ApiResponse.success("Facilities retrieved successfully", facilities));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}