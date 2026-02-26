package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.BadRequestException;
import com.mpp.rental.service.MPPFacilityApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mpp/approvals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MPPFacilityApprovalController {

    private final MPPFacilityApprovalService approvalService;

    // ==================== EVENT LIST ====================

    /**
     * GET /api/mpp/approvals/events
     * Get all events with application summary (pending/approved/rejected counts)
     */
    @GetMapping("/events")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<EventApprovalSummaryResponse>>> getEventsWithSummary(
            @RequestParam(defaultValue = "all") String statusFilter) {
        try {
            List<EventApprovalSummaryResponse> events = approvalService.getEventsWithApplicationSummary(statusFilter);
            return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== APPLICATIONS BY EVENT ====================

    /**
     * GET /api/mpp/approvals/events/{eventId}/applications
     * Get all applications for a specific event
     */
    @GetMapping("/events/{eventId}/applications")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<MPPApplicationResponse>>> getApplicationsByEvent(
            @PathVariable Integer eventId,
            @RequestParam(defaultValue = "all") String statusFilter,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "latest") String sortOrder) {
        try {
            List<MPPApplicationResponse> applications = approvalService.getApplicationsByEvent(
                    eventId, statusFilter, searchQuery, sortOrder);
            return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", applications));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== SINGLE ACTIONS ====================

    /**
     * PATCH /api/mpp/approvals/{applicationId}/approve
     */
    @PatchMapping("/{applicationId}/approve")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<MPPApplicationResponse>> approveApplication(
            @PathVariable Integer applicationId) {
        try {
            MPPApplicationResponse response = approvalService.approveApplication(applicationId);
            return ResponseEntity.ok(ApiResponse.success("Application approved successfully", response));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /api/mpp/approvals/{applicationId}/reject
     * Body: { "rejectionReason": "optional reason" }
     */
    @PatchMapping("/{applicationId}/reject")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<MPPApplicationResponse>> rejectApplication(
            @PathVariable Integer applicationId,
            @RequestBody(required = false) MPPApplicationActionRequest request) {
        try {
            String reason = (request != null) ? request.getRejectionReason() : null;
            MPPApplicationResponse response = approvalService.rejectApplication(applicationId, reason);
            return ResponseEntity.ok(ApiResponse.success("Application rejected successfully", response));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /api/mpp/approvals/{applicationId}/revert
     * Revert APPROVED or REJECTED back to PENDING
     */
    @PatchMapping("/{applicationId}/revert")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<MPPApplicationResponse>> revertToPending(
            @PathVariable Integer applicationId) {
        try {
            MPPApplicationResponse response = approvalService.revertToPending(applicationId);
            return ResponseEntity.ok(ApiResponse.success("Application reverted to pending successfully", response));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/mpp/approvals/{applicationId}/payment-status
     * Check if application has been paid (for revert confirmation warning)
     */
    @GetMapping("/{applicationId}/payment-status")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkPaymentStatus(
            @PathVariable Integer applicationId) {
        try {
            boolean hasPaid = approvalService.hasBeenPaid(applicationId);
            return ResponseEntity.ok(ApiResponse.success("Payment status retrieved",
                    Map.of("hasPaid", hasPaid)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== BULK ACTIONS ====================

    /**
     * POST /api/mpp/approvals/bulk-approve
     * Body: { "applicationIds": [1, 2, 3] }
     */
    @PostMapping("/bulk-approve")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<MPPApplicationResponse>>> bulkApprove(
            @RequestBody MPPApplicationActionRequest request) {
        try {
            List<MPPApplicationResponse> results = approvalService.bulkApprove(request.getApplicationIds());
            return ResponseEntity.ok(ApiResponse.success(
                    results.size() + " application(s) approved successfully", results));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/mpp/approvals/bulk-reject
     * Body: { "applicationIds": [1, 2, 3], "rejectionReason": "optional" }
     */
    @PostMapping("/bulk-reject")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<MPPApplicationResponse>>> bulkReject(
            @RequestBody MPPApplicationActionRequest request) {
        try {
            List<MPPApplicationResponse> results = approvalService.bulkReject(
                    request.getApplicationIds(), request.getRejectionReason());
            return ResponseEntity.ok(ApiResponse.success(
                    results.size() + " application(s) rejected successfully", results));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/mpp/approvals/bulk-revert
     * Body: { "applicationIds": [1, 2, 3] }
     */
    @PostMapping("/bulk-revert")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<MPPApplicationResponse>>> bulkRevert(
            @RequestBody MPPApplicationActionRequest request) {
        try {
            List<MPPApplicationResponse> results = approvalService.bulkRevert(request.getApplicationIds());
            return ResponseEntity.ok(ApiResponse.success(
                    results.size() + " application(s) reverted successfully", results));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}