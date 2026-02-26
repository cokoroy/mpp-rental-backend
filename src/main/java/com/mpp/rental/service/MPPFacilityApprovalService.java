package com.mpp.rental.service;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.BadRequestException;
import com.mpp.rental.exception.ResourceNotFoundException;
import com.mpp.rental.model.*;
import com.mpp.rental.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MPPFacilityApprovalService {

    private final FacilityApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final EventFacilityRepository eventFacilityRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    // ==================== EVENT LIST ====================

    /**
     * Get all events with application summary counts for approval page
     */
    @Transactional(readOnly = true)
    public List<EventApprovalSummaryResponse> getEventsWithApplicationSummary(String statusFilter) {
        List<Event> events = eventRepository.findAll();

        return events.stream()
                .filter(e -> e.getDeletedAt() == null)
                .filter(e -> !e.getEventStatus().equals("cancelled"))
                .filter(e -> {
                    if (statusFilter != null && !statusFilter.equals("all")) {
                        return e.getEventStatus().equals(statusFilter);
                    }
                    return true;
                })
                .map(this::buildEventApprovalSummary)
                .collect(Collectors.toList());
    }

    private EventApprovalSummaryResponse buildEventApprovalSummary(Event event) {
        // Get all applications for this event
        List<FacilityApplication> apps = applicationRepository.findAllByEventId(event.getEventId());

        int pending = (int) apps.stream()
                .filter(a -> a.getApplicationStatus() == FacilityApplication.ApplicationStatus.PENDING)
                .count();
        int approved = (int) apps.stream()
                .filter(a -> a.getApplicationStatus() == FacilityApplication.ApplicationStatus.APPROVED)
                .count();
        int rejected = (int) apps.stream()
                .filter(a -> a.getApplicationStatus() == FacilityApplication.ApplicationStatus.REJECTED)
                .count();

        EventApprovalSummaryResponse response = new EventApprovalSummaryResponse();
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventVenue(event.getEventVenue());
        response.setEventStartDate(event.getEventStartDate().format(DATE_FORMATTER));
        response.setEventEndDate(event.getEventEndDate().format(DATE_FORMATTER));
        response.setEventStatus(event.getEventStatus());
        response.setEventApplicationStatus(event.getEventApplicationStatus());
        response.setTotalApplications(apps.size());
        response.setPendingCount(pending);
        response.setApprovedCount(approved);
        response.setRejectedCount(rejected);

        return response;
    }

    // ==================== APPLICATIONS BY EVENT ====================

    /**
     * Get all applications for a specific event with optional status filter
     */
    @Transactional(readOnly = true)
    public List<MPPApplicationResponse> getApplicationsByEvent(Integer eventId, String statusFilter, String searchQuery, String sortOrder) {
        // Validate event exists
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        List<FacilityApplication> apps = applicationRepository.findAllByEventIdWithDetails(eventId);

        return apps.stream()
                .filter(app -> {
                    if (statusFilter != null && !statusFilter.equals("all")) {
                        return app.getApplicationStatus().name().equalsIgnoreCase(statusFilter);
                    }
                    return true;
                })
                .filter(app -> {
                    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                        String q = searchQuery.toLowerCase();
                        String businessName = app.getBusiness().getBusinessName().toLowerCase();
                        String ownerName = app.getBusiness().getUser().getUserName().toLowerCase();
                        return businessName.contains(q) || ownerName.contains(q);
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    if ("oldest".equals(sortOrder)) {
                        return a.getApplicationCreatedAt().compareTo(b.getApplicationCreatedAt());
                    }
                    return b.getApplicationCreatedAt().compareTo(a.getApplicationCreatedAt());
                })
                .map(app -> {
                    Optional<Payment> payment = paymentRepository.findByApplication_ApplicationId(app.getApplicationId());
                    return mapToMPPResponse(app, payment.orElse(null));
                })
                .collect(Collectors.toList());
    }

    // ==================== SINGLE APPROVE ====================

    /**
     * Approve a single application
     * - Deducts quantityFacilityAvailable in EventFacility
     * - Creates Payment record if amount > 0
     * - Auto-rejects other PENDING apps if quota hits 0
     */
    @Transactional
    public MPPApplicationResponse approveApplication(Integer applicationId) {
        FacilityApplication app = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (app.getApplicationStatus() != FacilityApplication.ApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be approved");
        }

        EventFacility ef = app.getEventFacility();

        // Check available quota
        if (ef.getQuantityFacilityAvailable() < app.getApplicationFacilityQuantity()) {
            throw new BadRequestException("Insufficient facility quota. Available: "
                    + ef.getQuantityFacilityAvailable()
                    + ", Requested: " + app.getApplicationFacilityQuantity());
        }

        // Approve
        app.setApplicationStatus(FacilityApplication.ApplicationStatus.APPROVED);
        applicationRepository.save(app);

        // Deduct quota
        ef.setQuantityFacilityAvailable(ef.getQuantityFacilityAvailable() - app.getApplicationFacilityQuantity());
        eventFacilityRepository.save(ef);

        // Create payment record if amount > 0
        Payment payment = createPaymentIfNeeded(app, ef);

        // Auto-reject remaining PENDING if quota hits 0
        if (ef.getQuantityFacilityAvailable() == 0) {
            autoRejectRemainingPending(ef.getEventFacilityId(), applicationId);
        }

        return mapToMPPResponse(app, payment);
    }

    // ==================== SINGLE REJECT ====================

    /**
     * Reject a single application with optional reason
     */
    @Transactional
    public MPPApplicationResponse rejectApplication(Integer applicationId, String rejectionReason) {
        FacilityApplication app = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (app.getApplicationStatus() != FacilityApplication.ApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be rejected");
        }

        app.setApplicationStatus(FacilityApplication.ApplicationStatus.REJECTED);
        app.setRejectionReason(rejectionReason);
        applicationRepository.save(app);

        return mapToMPPResponse(app, null);
    }

    // ==================== REVERT TO PENDING ====================

    /**
     * Revert an APPROVED or REJECTED application back to PENDING
     * - If APPROVED: deletes UNPAID payment record, restores quota
     * - If REJECTED: just status change
     */
    @Transactional
    public MPPApplicationResponse revertToPending(Integer applicationId) {
        FacilityApplication app = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        FacilityApplication.ApplicationStatus currentStatus = app.getApplicationStatus();

        if (currentStatus == FacilityApplication.ApplicationStatus.PENDING) {
            throw new BadRequestException("Application is already PENDING");
        }

        if (currentStatus == FacilityApplication.ApplicationStatus.APPROVED) {
            // Delete UNPAID payment record if exists
            Optional<Payment> payment = paymentRepository.findByApplication_ApplicationId(applicationId);
            payment.ifPresent(p -> {
                if (p.getPaymentStatus() == Payment.PaymentStatus.UNPAID) {
                    paymentRepository.delete(p);
                }
                // If PAID, don't delete — keep it but still revert status
            });

            // Restore quota
            EventFacility ef = app.getEventFacility();
            ef.setQuantityFacilityAvailable(ef.getQuantityFacilityAvailable() + app.getApplicationFacilityQuantity());
            eventFacilityRepository.save(ef);
        }

        // Revert status
        app.setApplicationStatus(FacilityApplication.ApplicationStatus.PENDING);
        app.setRejectionReason(null);
        applicationRepository.save(app);

        return mapToMPPResponse(app, null);
    }

    // ==================== BULK APPROVE ====================

    @Transactional
    public List<MPPApplicationResponse> bulkApprove(List<Integer> applicationIds) {
        List<MPPApplicationResponse> results = new ArrayList<>();
        for (Integer id : applicationIds) {
            try {
                results.add(approveApplication(id));
            } catch (Exception e) {
                log.warn("Failed to approve application {}: {}", id, e.getMessage());
            }
        }
        return results;
    }

    // ==================== BULK REJECT ====================

    @Transactional
    public List<MPPApplicationResponse> bulkReject(List<Integer> applicationIds, String rejectionReason) {
        List<MPPApplicationResponse> results = new ArrayList<>();
        for (Integer id : applicationIds) {
            try {
                results.add(rejectApplication(id, rejectionReason));
            } catch (Exception e) {
                log.warn("Failed to reject application {}: {}", id, e.getMessage());
            }
        }
        return results;
    }

    // ==================== BULK REVERT ====================

    @Transactional
    public List<MPPApplicationResponse> bulkRevert(List<Integer> applicationIds) {
        List<MPPApplicationResponse> results = new ArrayList<>();
        for (Integer id : applicationIds) {
            try {
                results.add(revertToPending(id));
            } catch (Exception e) {
                log.warn("Failed to revert application {}: {}", id, e.getMessage());
            }
        }
        return results;
    }

    // ==================== CHECK PAYMENT STATUS (for revert confirmation) ====================

    /**
     * Check if an approved application has a PAID payment
     * Used by frontend to warn MPP before reverting
     */
    @Transactional(readOnly = true)
    public boolean hasBeenPaid(Integer applicationId) {
        Optional<Payment> payment = paymentRepository.findByApplication_ApplicationId(applicationId);
        return payment.map(p -> p.getPaymentStatus() == Payment.PaymentStatus.PAID).orElse(false);
    }

    // ==================== AUTO-REJECT ON BLOCK ====================

    /**
     * Auto-reject all PENDING applications for a blocked business owner
     */
    @Transactional
    public void autoRejectPendingForBlockedUser(Long userId) {
        List<FacilityApplication> pendingApps = applicationRepository.findPendingByUserId(userId);
        for (FacilityApplication app : pendingApps) {
            app.setApplicationStatus(FacilityApplication.ApplicationStatus.REJECTED);
            app.setRejectionReason("Application rejected due to blocked account");
            applicationRepository.save(app);
        }
        log.info("Auto-rejected {} pending applications for blocked user {}", pendingApps.size(), userId);
    }

    // ==================== HELPERS ====================

    private Payment createPaymentIfNeeded(FacilityApplication app, EventFacility ef) {
        User owner = app.getBusiness().getUser();

        // Determine price based on user category
        BigDecimal pricePerUnit = User.UserCategory.STUDENT.equals(owner.getUserCategory())
                ? ef.getFacilityStudentPrice()
                : ef.getFacilityNonStudentPrice();

        BigDecimal totalAmount = pricePerUnit.multiply(BigDecimal.valueOf(app.getApplicationFacilityQuantity()));

        // Only create payment if amount > 0
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = new Payment();
            payment.setApplication(app);
            payment.setPaymentAmount(totalAmount);
            payment.setPaymentStatus(Payment.PaymentStatus.UNPAID);
            return paymentRepository.save(payment);
        }

        return null;
    }

    private void autoRejectRemainingPending(Integer eventFacilityId, Integer approvedApplicationId) {
        List<FacilityApplication> pendingApps = applicationRepository
                .findPendingByEventFacilityId(eventFacilityId);

        for (FacilityApplication app : pendingApps) {
            if (!app.getApplicationId().equals(approvedApplicationId)) {
                app.setApplicationStatus(FacilityApplication.ApplicationStatus.REJECTED);
                app.setRejectionReason("Facility quota has been filled");
                applicationRepository.save(app);
                log.info("Auto-rejected application {} due to quota exhaustion", app.getApplicationId());
            }
        }
    }

    private MPPApplicationResponse mapToMPPResponse(FacilityApplication app, Payment payment) {
        EventFacility ef = app.getEventFacility();
        Event event = ef.getEvent();
        Business business = app.getBusiness();
        User owner = business.getUser();
        Facility facility = ef.getFacility();

        MPPApplicationResponse response = new MPPApplicationResponse();
        response.setApplicationId(app.getApplicationId());

        // Business
        response.setBusinessId(business.getBusinessId());
        response.setBusinessName(business.getBusinessName());
        response.setBusinessCategory(business.getBusinessCategory());
        response.setBusinessDesc(business.getBusinessDesc());
        response.setBusinessStatus(business.getBusinessStatus());

        // Owner
        response.setOwnerId(owner.getUserId());
        response.setOwnerName(owner.getUserName());
        response.setOwnerEmail(owner.getUserEmail());
        response.setOwnerCategory(owner.getUserCategory().name());

        // Event
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventVenue(event.getEventVenue());
        response.setEventStartDate(event.getEventStartDate().format(DATE_FORMATTER));
        response.setEventEndDate(event.getEventEndDate().format(DATE_FORMATTER));
        response.setEventStatus(event.getEventStatus());

        // Facility
        response.setEventFacilityId(ef.getEventFacilityId());
        response.setFacilityName(facility.getFacilityName());
        response.setFacilitySize(facility.getFacilitySize());
        response.setFacilityType(facility.getFacilityType());

        // Application
        response.setApplicationFacilityQuantity(app.getApplicationFacilityQuantity());
        response.setApplicationStatus(app.getApplicationStatus().name());
        response.setApplicationCreatedAt(app.getApplicationCreatedAt());
        response.setRejectionReason(app.getRejectionReason());

        // Payment
        if (payment != null) {
            response.setPaymentId(payment.getPaymentId());
            response.setPaymentAmount(payment.getPaymentAmount());
            response.setPaymentStatus(payment.getPaymentStatus().name());
        }

        return response;
    }
}