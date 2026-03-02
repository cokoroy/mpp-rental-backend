package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.BadRequestException;
import com.mpp.rental.exception.ResourceNotFoundException;
import com.mpp.rental.exception.SupportTicketException;
import com.mpp.rental.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    // ==================== BUSINESS OWNER ENDPOINTS ====================

    /**
     * POST /api/bo/support
     * Create a new support ticket
     */
    @PostMapping("/api/bo/support")
    @PreAuthorize("hasAnyRole('STUDENT', 'NON_STUDENT')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        try {
            SupportTicketResponse response = supportTicketService.createTicket(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Ticket created successfully", response));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/bo/support
     * Get all tickets submitted by the current Business Owner
     */
    @GetMapping("/api/bo/support")
    @PreAuthorize("hasAnyRole('STUDENT', 'NON_STUDENT')")
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> getMyTickets() {
        try {
            List<SupportTicketResponse> tickets = supportTicketService.getMyTickets();
            return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/bo/support/{ticketId}
     * Get a single ticket with conversation thread (Business Owner - own ticket only)
     */
    @GetMapping("/api/bo/support/{ticketId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'NON_STUDENT')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> getMyTicketById(
            @PathVariable Integer ticketId) {
        try {
            SupportTicketResponse response = supportTicketService.getMyTicketById(ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/bo/support/{ticketId}/reply
     * Business Owner replies to an existing ticket
     */
    @PostMapping("/api/bo/support/{ticketId}/reply")
    @PreAuthorize("hasAnyRole('STUDENT', 'NON_STUDENT')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> replyToTicket(
            @PathVariable Integer ticketId,
            @Valid @RequestBody CreateTicketResponseRequest request) {
        try {
            SupportTicketResponse response = supportTicketService.replyToTicket(ticketId, request);
            return ResponseEntity.ok(ApiResponse.success("Reply sent successfully", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SupportTicketException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/bo/support/{ticketId}
     * Delete a ticket (only allowed if status is OPEN)
     */
    @DeleteMapping("/api/bo/support/{ticketId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'NON_STUDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(
            @PathVariable Integer ticketId) {
        try {
            supportTicketService.deleteTicket(ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SupportTicketException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== MPP ENDPOINTS ====================

    /**
     * GET /api/mpp/support
     * Get all tickets with optional filters (MPP)
     * @param status  - all | OPEN | IN_PROGRESS | RESOLVED
     * @param priority - all | LOW | MEDIUM | HIGH
     * @param category - all | PAYMENT | TECHNICAL | APPLICATION | GENERAL
     */
    @GetMapping("/api/mpp/support")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> getAllTickets(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String priority,
            @RequestParam(defaultValue = "all") String category) {
        try {
            List<SupportTicketResponse> tickets = supportTicketService.getAllTickets(status, priority, category);
            return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/mpp/support/{ticketId}
     * Get a single ticket with conversation thread (MPP - any ticket)
     */
    @GetMapping("/api/mpp/support/{ticketId}")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> getTicketById(
            @PathVariable Integer ticketId) {
        try {
            SupportTicketResponse response = supportTicketService.getTicketById(ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/mpp/support/{ticketId}/reply
     * MPP replies to a ticket (auto-transitions OPEN → IN_PROGRESS)
     */
    @PostMapping("/api/mpp/support/{ticketId}/reply")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> mppReplyToTicket(
            @PathVariable Integer ticketId,
            @Valid @RequestBody CreateTicketResponseRequest request) {
        try {
            SupportTicketResponse response = supportTicketService.mppReplyToTicket(ticketId, request);
            return ResponseEntity.ok(ApiResponse.success("Reply sent successfully", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SupportTicketException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /api/mpp/support/{ticketId}/resolve
     * Mark a ticket as RESOLVED
     */
    @PatchMapping("/api/mpp/support/{ticketId}/resolve")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> resolveTicket(
            @PathVariable Integer ticketId) {
        try {
            SupportTicketResponse response = supportTicketService.resolveTicket(ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket marked as resolved", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SupportTicketException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /api/mpp/support/{ticketId}/reopen
     * Reopen a RESOLVED ticket → back to IN_PROGRESS
     */
    @PatchMapping("/api/mpp/support/{ticketId}/reopen")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> reopenTicket(
            @PathVariable Integer ticketId) {
        try {
            SupportTicketResponse response = supportTicketService.reopenTicket(ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket reopened successfully", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SupportTicketException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /api/mpp/support/{ticketId}/priority
     * Update ticket priority (MPP only)
     */
    @PatchMapping("/api/mpp/support/{ticketId}/priority")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> updateTicketPriority(
            @PathVariable Integer ticketId,
            @Valid @RequestBody UpdateTicketPriorityRequest request) {
        try {
            SupportTicketResponse response = supportTicketService.updateTicketPriority(ticketId, request);
            return ResponseEntity.ok(ApiResponse.success("Ticket priority updated successfully", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}