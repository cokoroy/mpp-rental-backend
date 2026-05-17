package com.mpp.rental.service;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.ApplicationException;
import com.mpp.rental.exception.BadRequestException;
import com.mpp.rental.exception.ResourceNotFoundException;
import com.mpp.rental.model.*;
import com.mpp.rental.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityApplicationService {

    private final FacilityApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final EventFacilityRepository eventFacilityRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // ← ADDED

    // ==================== BO EVENT BROWSING ====================

    /**
     * Get all events for Business Owner view
     * - completed events disappear after 3 days
     * - cancelled events not shown
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsForBO(String searchQuery, String eventStatus) {
        List<Event> allEvents = eventRepository.findAll();

        LocalDate cutoff = LocalDate.now().minusDays(3);

        return allEvents.stream()
                .filter(e -> e.getDeletedAt() == null)
                .filter(e -> !"cancelled".equals(e.getEventStatus()))
                .filter(e -> {
                    // Hide completed events older than 3 days
                    if ("completed".equals(e.getEventStatus())) {
                        return !e.getEventEndDate().isBefore(cutoff);
                    }
                    return true;
                })
                .filter(e -> {
                    // Search filter
                    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                        String q = searchQuery.toLowerCase();
                        return e.getEventName().toLowerCase().contains(q)
                                || e.getEventVenue().toLowerCase().contains(q);
                    }
                    return true;
                })
                .filter(e -> {
                    // Status filter
                    if (eventStatus != null && !eventStatus.equals("all")) {
                        return e.getEventStatus().equals(eventStatus);
                    }
                    return true;
                })
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get event with facilities for BO — includes applicablePrice and quota info
     */
    @Transactional(readOnly = true)
    public BOEventWithFacilitiesResponse getEventWithFacilitiesForBO(Integer eventId) {
        // Get current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findByEventIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        // Update event status
        event.updateStatusBasedOnDates();
        eventRepository.save(event);

        List<EventFacility> facilities = eventFacilityRepository.findByEventIdWithFacility(eventId);

        // Get all active businesses of current user for quota checks
        List<Business> userBusinesses = businessRepository.findByUser_UserId(user.getUserId());

        List<BOEventFacilityResponse> facilityResponses = facilities.stream()
                .map(ef -> mapToBOEventFacilityResponse(ef, user, userBusinesses))
                .collect(Collectors.toList());

        BOEventWithFacilitiesResponse response = new BOEventWithFacilitiesResponse();
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventVenue(event.getEventVenue());
        response.setEventStartDate(event.getEventStartDate());
        response.setEventEndDate(event.getEventEndDate());
        response.setEventStartTime(event.getEventStartTime());
        response.setEventEndTime(event.getEventEndTime());
        response.setEventType(event.getEventType());
        response.setEventDesc(event.getEventDesc());
        response.setEventApplicationStatus(event.getEventApplicationStatus());
        response.setEventStatus(event.getEventStatus());
        response.setEventCreateAt(event.getEventCreateAt());
        response.setFacilities(facilityResponses);

        return response;
    }

    // ==================== APPLICATION SUBMISSION ====================

    /**
     * Submit facility applications for a business owner
     * Creates one FACILITY_APPLICATION record per selected facility
     */
    @Transactional
    public List<FacilityApplicationResponse> submitApplications(CreateApplicationRequest request) {
        // Get current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate business belongs to current user
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        if (!business.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Business does not belong to current user");
        }

        if (!"ACTIVE".equals(business.getBusinessStatus())) {
            throw new BadRequestException("Business is not active");
        }

        // Validate each facility and create application records
        List<FacilityApplication> savedApplications = new ArrayList<>();

        for (CreateApplicationRequest.FacilityApplicationItem item : request.getFacilities()) {
            EventFacility eventFacility = eventFacilityRepository.findById(item.getEventFacilityId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Event facility not found with ID: " + item.getEventFacilityId()));

            // Validate event application is OPEN
            Event event = eventFacility.getEvent();
            if (!"OPEN".equals(event.getEventApplicationStatus())) {
                throw new ApplicationException("Applications are closed for event: " + event.getEventName());
            }

            // Check for existing PENDING application
            boolean hasPending = applicationRepository.hasPendingApplication(
                    business.getBusinessId(), item.getEventFacilityId());
            if (hasPending) {
                throw new ApplicationException(
                        "You already have a pending application for facility: "
                                + eventFacility.getFacility().getFacilityName()
                                + ". Please wait for it to be reviewed.");
            }

            // Check quota: total (PENDING + APPROVED) + new request <= maxPerBusiness
            Integer totalApplied = applicationRepository.getTotalAppliedQuantity(
                    business.getBusinessId(), item.getEventFacilityId());
            int remaining = eventFacility.getMaxPerBusiness() - totalApplied;

            if (item.getQuantity() > remaining) {
                throw new ApplicationException(
                        "Insufficient quota for facility: " + eventFacility.getFacility().getFacilityName()
                                + ". You can only apply for " + remaining + " more unit(s).");
            }

            // Create application record
            FacilityApplication application = new FacilityApplication();
            application.setBusiness(business);
            application.setEventFacility(eventFacility);
            application.setApplicationFacilityQuantity(item.getQuantity());
            application.setApplicationStatus(FacilityApplication.ApplicationStatus.PENDING);

            savedApplications.add(applicationRepository.save(application));
        }

        // ── NOTIFICATION: notify MPP of the new application(s) ──────────────
        // Use first application as reference (one submission = one notification)
        if (!savedApplications.isEmpty()) {
            FacilityApplication first = savedApplications.get(0);
            Event event = first.getEventFacility().getEvent();
            try {
                notificationService.notifyApplicationSubmitted(
                        first.getApplicationId().longValue(),
                        business.getBusinessName(),
                        event.getEventName()
                );
            } catch (Exception e) {
                log.warn("Failed to send notification for application submission: {}", e.getMessage());
            }
        }
        // ────────────────────────────────────────────────────────────────────


        return savedApplications.stream()
                .map(app -> mapToApplicationResponse(app, null))
                .collect(Collectors.toList());
    }

    /**
     * Get all applications for the logged-in Business Owner
     * GET /api/bo/applications
     */
    @Transactional(readOnly = true)
    public List<FacilityApplicationResponse> getMyApplications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<FacilityApplication> applications = applicationRepository.findAllByUserId(user.getUserId());

        return applications.stream()
                .map(app -> {
                    Payment payment = paymentRepository
                            .findByApplication_ApplicationId(app.getApplicationId())
                            .orElse(null);
                    return mapToApplicationResponse(app, payment);
                })
                .collect(Collectors.toList());
    }

    /**
     * Cancel a PENDING application — sets status to CANCELLED
     * DELETE /api/bo/applications/{id}/cancel
     */
    @Transactional
    public void cancelApplication(Integer applicationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FacilityApplication application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        // Verify the application belongs to the current user
        if (!application.getBusiness().getUser().getUserId().equals(user.getUserId())) {
            throw new ApplicationException("You do not have permission to cancel this application");
        }

        // Only PENDING applications can be cancelled
        if (application.getApplicationStatus() != FacilityApplication.ApplicationStatus.PENDING) {
            throw new ApplicationException("Only PENDING applications can be cancelled");
        }

        application.setApplicationStatus(FacilityApplication.ApplicationStatus.CANCELLED);
        applicationRepository.save(application);
    }

    /**
     * Delete a REJECTED or CANCELLED application — hard delete from DB
     * DELETE /api/bo/applications/{id}
     */
    @Transactional
    public void deleteApplication(Integer applicationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FacilityApplication application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        // Verify the application belongs to the current user
        if (!application.getBusiness().getUser().getUserId().equals(user.getUserId())) {
            throw new ApplicationException("You do not have permission to delete this application");
        }

        // Only REJECTED or CANCELLED applications can be deleted
        if (application.getApplicationStatus() != FacilityApplication.ApplicationStatus.REJECTED
                && application.getApplicationStatus() != FacilityApplication.ApplicationStatus.CANCELLED) {
            throw new ApplicationException("Only REJECTED or CANCELLED applications can be deleted");
        }

        applicationRepository.delete(application);
    }

    // ==================== HELPER MAPPERS ====================

    private EventResponse mapToEventResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventVenue(event.getEventVenue());
        response.setEventStartDate(event.getEventStartDate());
        response.setEventEndDate(event.getEventEndDate());
        response.setEventStartTime(event.getEventStartTime());
        response.setEventEndTime(event.getEventEndTime());
        response.setEventType(event.getEventType());
        response.setEventDesc(event.getEventDesc());
        response.setEventApplicationStatus(event.getEventApplicationStatus());
        response.setEventStatus(event.getEventStatus());
        response.setEventCreateAt(event.getEventCreateAt());
        return response;
    }

    private BOEventFacilityResponse mapToBOEventFacilityResponse(
            EventFacility ef, User user, List<Business> userBusinesses) {

        Facility facility = ef.getFacility();

        // Determine applicable price based on user category
        BigDecimal applicablePrice = User.UserCategory.STUDENT.equals(user.getUserCategory())
                ? ef.getFacilityStudentPrice()
                : ef.getFacilityNonStudentPrice();

        int minRemainingQuota = ef.getMaxPerBusiness();
        boolean anyPending = false;

        for (Business b : userBusinesses) {
            Integer totalApplied = applicationRepository.getTotalAppliedQuantity(
                    b.getBusinessId(), ef.getEventFacilityId());
            int remaining = ef.getMaxPerBusiness() - totalApplied;
            if (remaining < minRemainingQuota) {
                minRemainingQuota = remaining;
            }
            if (applicationRepository.hasPendingApplication(b.getBusinessId(), ef.getEventFacilityId())) {
                anyPending = true;
            }
        }

        BOEventFacilityResponse response = new BOEventFacilityResponse();
        response.setEventFacilityId(ef.getEventFacilityId());
        response.setFacilityId(facility.getFacilityId());
        response.setFacilityName(facility.getFacilityName());
        response.setFacilitySize(facility.getFacilitySize());
        response.setFacilityType(facility.getFacilityType());
        response.setFacilityDesc(facility.getFacilityDesc());
        response.setFacilityUsage(facility.getFacility_usage());
        response.setFacilityRemark(facility.getRemark());
        response.setFacilityImage(facility.getFacilityImage());
        response.setQuantityFacilityAvailable(ef.getQuantityFacilityAvailable());
        response.setFacilityStudentPrice(ef.getFacilityStudentPrice());
        response.setFacilityNonStudentPrice(ef.getFacilityNonStudentPrice());
        response.setApplicablePrice(applicablePrice);
        response.setMaxPerBusiness(ef.getMaxPerBusiness());
        response.setRemainingQuota(minRemainingQuota);
        response.setHasPendingApplication(anyPending);

        return response;
    }

    private FacilityApplicationResponse mapToApplicationResponse(
            FacilityApplication app, Payment payment) {

        EventFacility ef = app.getEventFacility();
        Event event = ef.getEvent();
        Business business = app.getBusiness();
        Facility facility = ef.getFacility();

        FacilityApplicationResponse response = new FacilityApplicationResponse();
        response.setApplicationId(app.getApplicationId());
        response.setBusinessId(business.getBusinessId());
        response.setBusinessName(business.getBusinessName());
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventVenue(event.getEventVenue());
        response.setEventFacilityId(ef.getEventFacilityId());
        response.setFacilityName(facility.getFacilityName());
        response.setFacilitySize(facility.getFacilitySize());
        response.setApplicationFacilityQuantity(app.getApplicationFacilityQuantity());
        response.setApplicationStatus(app.getApplicationStatus().name());
        response.setApplicationCreatedAt(app.getApplicationCreatedAt());
        response.setRejectionReason(app.getRejectionReason());

        if (payment != null) {
            response.setPaymentId(payment.getPaymentId());
            response.setPaymentAmount(payment.getPaymentAmount());
            response.setPaymentStatus(payment.getPaymentStatus().name());
        }

        return response;
    }
}