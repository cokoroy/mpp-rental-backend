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

    // ==================== DROPDOWN DATA (shared across all report types) ====================

    @GetMapping("/events")
    @PreAuthorize("hasRole('MPP') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsForFilter() {
        try {
            return ResponseEntity.ok(ApiResponse.success("Events retrieved", reportService.getEventsForFilter()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/facilities")
    @PreAuthorize("hasRole('MPP') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getFacilitiesForFilter() {
        try {
            return ResponseEntity.ok(ApiResponse.success("Facilities retrieved", reportService.getFacilitiesForFilter()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== REPORT 1: FACILITY RENTAL ====================

    /**
     * GET /api/mpp/reports/facility-rental
     * One row per application. Filters: event, facility, ownerCategory, applicationStatus, paymentStatus, dateRange.
     */
    @GetMapping("/facility-rental")
    @PreAuthorize("hasRole('MPP') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse<FacilityRentalSummary, FacilityRentalRow>>> getFacilityRentalReport(
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) Integer facilityId,
            @RequestParam(required = false) String ownerCategory,
            @RequestParam(required = false) String applicationStatus,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            ReportResponse<FacilityRentalSummary, FacilityRentalRow> data =
                    reportService.getFacilityRentalReport(eventId, facilityId, ownerCategory,
                            applicationStatus, paymentStatus, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Report generated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== REPORT 2: REVENUE & PAYMENT ====================

    /**
     * GET /api/mpp/reports/revenue
     * One row per approved application that has a payment record.
     * Filters: event, ownerCategory, paymentStatus, dateRange (payment created date).
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('MPP') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse<RevenueSummary, RevenueRow>>> getRevenueReport(
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) String ownerCategory,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            ReportResponse<RevenueSummary, RevenueRow> data =
                    reportService.getRevenueReport(eventId, ownerCategory, paymentStatus, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Report generated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== REPORT 3: BUSINESS OWNER ACTIVITY ====================

    /**
     * GET /api/mpp/reports/business-activity
     * One row per business (aggregated). Filters: ownerCategory, eventId, dateRange.
     */
    @GetMapping("/business-activity")
    @PreAuthorize("hasRole('MPP') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse<BusinessActivitySummary, BusinessActivityRow>>> getBusinessActivityReport(
            @RequestParam(required = false) String ownerCategory,
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            ReportResponse<BusinessActivitySummary, BusinessActivityRow> data =
                    reportService.getBusinessActivityReport(ownerCategory, eventId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Report generated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== REPORT 4: EVENT PERFORMANCE ====================

    /**
     * GET /api/mpp/reports/event-performance
     * One row per event (aggregated). Filters: eventStatus, dateRange (event start date).
     */
    @GetMapping("/event-performance")
    @PreAuthorize("hasRole('MPP') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse<EventPerformanceSummary, EventPerformanceRow>>> getEventPerformanceReport(
            @RequestParam(required = false) String eventStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            ReportResponse<EventPerformanceSummary, EventPerformanceRow> data =
                    reportService.getEventPerformanceReport(eventStatus, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Report generated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== REPORT 5: FACILITY UTILISATION ====================

    /**
     * GET /api/mpp/reports/facility-utilisation
     * One row per facility template (aggregated across events).
     * Filters: facilityType, eventId, dateRange (event start date).
     */
    @GetMapping("/facility-utilisation")
    @PreAuthorize("hasRole('MPP') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse<FacilityUtilisationSummary, FacilityUtilisationRow>>> getFacilityUtilisationReport(
            @RequestParam(required = false) String facilityType,
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            ReportResponse<FacilityUtilisationSummary, FacilityUtilisationRow> data =
                    reportService.getFacilityUtilisationReport(facilityType, eventId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Report generated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}