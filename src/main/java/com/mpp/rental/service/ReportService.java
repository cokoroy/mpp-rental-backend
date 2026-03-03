package com.mpp.rental.service;

import com.mpp.rental.dto.*;
import com.mpp.rental.model.*;
import com.mpp.rental.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final FacilityApplicationRepository applicationRepository;
    private final PaymentRepository             paymentRepository;
    private final EventRepository               eventRepository;
    private final FacilityRepository            facilityRepository;

    // ==================== GENERATE REPORT ====================

    /**
     * Generate facility rental report with optional filters.
     * All filters are optional — omitting them returns all records.
     */
    @Transactional(readOnly = true)
    public ReportResponse generateReport(ReportFilterRequest req) {

        // 1. Load all applications with associations eagerly loaded
        List<FacilityApplication> apps = applicationRepository.findAllForReport();

        // 2. Apply filters in-memory
        List<FacilityApplication> filtered = apps.stream()
                .filter(app -> filterByEvent(app, req.getEventId()))
                .filter(app -> filterByFacility(app, req.getFacilityId()))
                .filter(app -> filterByOwnerCategory(app, req.getOwnerCategory()))
                .filter(app -> filterByApplicationStatus(app, req.getApplicationStatus()))
                .filter(app -> filterByDateRange(app, req.getStartDate(), req.getEndDate()))
                .collect(Collectors.toList());

        // 3. Load payments for filtered applications
        List<Integer> appIds = filtered.stream()
                .map(FacilityApplication::getApplicationId)
                .collect(Collectors.toList());

        Map<Integer, Payment> paymentMap = paymentRepository.findAll().stream()
                .filter(p -> appIds.contains(p.getApplication().getApplicationId()))
                .collect(Collectors.toMap(
                        p -> p.getApplication().getApplicationId(),
                        p -> p,
                        (a, b) -> a
                ));

        // 4. Apply payment status filter (requires payment data)
        if (req.getPaymentStatus() != null && !req.getPaymentStatus().isBlank()) {
            filtered = filtered.stream()
                    .filter(app -> {
                        Payment payment = paymentMap.get(app.getApplicationId());
                        if (payment == null) return false;
                        return payment.getPaymentStatus().name().equalsIgnoreCase(req.getPaymentStatus());
                    })
                    .collect(Collectors.toList());
        }

        // 5. Build rows
        List<ReportRowResponse> rows = filtered.stream()
                .map(app -> mapToRow(app, paymentMap.get(app.getApplicationId())))
                .collect(Collectors.toList());

        // 6. Build summary
        ReportSummaryResponse summary = buildSummary(filtered, paymentMap);

        return new ReportResponse(summary, rows);
    }

    // ==================== DROPDOWN DATA ====================

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsForFilter() {
        return eventRepository.findAll().stream()
                .filter(e -> e.getDeletedAt() == null)
                .sorted(Comparator.comparing(Event::getEventName))
                .map(e -> {
                    EventResponse dto = new EventResponse();
                    dto.setEventId(e.getEventId());
                    dto.setEventName(e.getEventName());
                    dto.setEventStatus(e.getEventStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> getFacilitiesForFilter() {
        return facilityRepository.findAll().stream()
                .filter(f -> f.getDeletedAt() == null)
                .sorted(Comparator.comparing(Facility::getFacilityName))
                .map(f -> {
                    FacilityResponse dto = new FacilityResponse();
                    dto.setFacilityId(f.getFacilityId());
                    dto.setFacilityName(f.getFacilityName());
                    dto.setFacilityType(f.getFacilityType());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ==================== FILTER HELPERS ====================

    private boolean filterByEvent(FacilityApplication app, Integer eventId) {
        if (eventId == null) return true;
        return Objects.equals(app.getEventFacility().getEvent().getEventId(), eventId);
    }

    private boolean filterByFacility(FacilityApplication app, Integer facilityId) {
        if (facilityId == null) return true;
        return Objects.equals(app.getEventFacility().getFacility().getFacilityId(), facilityId);
    }

    private boolean filterByOwnerCategory(FacilityApplication app, String ownerCategory) {
        if (ownerCategory == null || ownerCategory.isBlank()) return true;
        return app.getBusiness().getUser().getUserCategory().name().equalsIgnoreCase(ownerCategory);
    }

    private boolean filterByApplicationStatus(FacilityApplication app, String status) {
        if (status == null || status.isBlank()) return true;
        return app.getApplicationStatus().name().equalsIgnoreCase(status);
    }

    private boolean filterByDateRange(FacilityApplication app, LocalDate start, LocalDate end) {
        if (start == null && end == null) return true;
        LocalDate appDate = app.getApplicationCreatedAt().toLocalDate();
        if (start != null && appDate.isBefore(start)) return false;
        if (end   != null && appDate.isAfter(end))    return false;
        return true;
    }

    // ==================== MAPPERS ====================

    private ReportRowResponse mapToRow(FacilityApplication app, Payment payment) {
        EventFacility ef       = app.getEventFacility();
        Event         event    = ef.getEvent();
        Facility      facility = ef.getFacility();
        Business      business = app.getBusiness();
        User          owner    = business.getUser();

        ReportRowResponse row = new ReportRowResponse();
        row.setApplicationId(app.getApplicationId());

        row.setEventName(event.getEventName());
        row.setEventVenue(event.getEventVenue());
        row.setEventStatus(event.getEventStatus());

        row.setFacilityName(facility.getFacilityName());

        row.setBusinessId(business.getBusinessId());
        row.setBusinessName(business.getBusinessName());

        row.setOwnerId(owner.getUserId());
        row.setOwnerName(owner.getUserName());
        row.setOwnerCategory(owner.getUserCategory().name());

        row.setApplicationStatus(app.getApplicationStatus().name());
        row.setApplicationCreatedAt(app.getApplicationCreatedAt());

        if (payment != null) {
            row.setPaymentStatus(payment.getPaymentStatus().name());
            row.setPaymentAmount(payment.getPaymentAmount());
        }

        return row;
    }

    private ReportSummaryResponse buildSummary(
            List<FacilityApplication> apps,
            Map<Integer, Payment> paymentMap) {

        int total      = apps.size();
        int approved   = (int) apps.stream().filter(a -> a.getApplicationStatus() == FacilityApplication.ApplicationStatus.APPROVED).count();
        int rejected   = (int) apps.stream().filter(a -> a.getApplicationStatus() == FacilityApplication.ApplicationStatus.REJECTED).count();
        int pending    = (int) apps.stream().filter(a -> a.getApplicationStatus() == FacilityApplication.ApplicationStatus.PENDING).count();
        int cancelled  = (int) apps.stream().filter(a -> a.getApplicationStatus() == FacilityApplication.ApplicationStatus.CANCELLED).count();

        int businesses = (int) apps.stream()
                .map(a -> a.getBusiness().getBusinessId())
                .distinct()
                .count();

        List<Payment> payments = new ArrayList<>(paymentMap.values());

        int paidCount = (int) payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.PAID)
                .count();

        BigDecimal totalRevenue = payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.PAID)
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnpaid = payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.UNPAID)
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportSummaryResponse(
                total, approved, rejected, pending, cancelled,
                businesses, paidCount, totalRevenue, totalUnpaid
        );
    }
}