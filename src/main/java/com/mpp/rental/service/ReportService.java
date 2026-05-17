package com.mpp.rental.service;

import com.mpp.rental.dto.*;
import com.mpp.rental.model.*;
import com.mpp.rental.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final EventFacilityRepository       eventFacilityRepository;

    // ==================== SHARED: DROPDOWN DATA ====================

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsForFilter() {
        return eventRepository.findAll().stream()
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

    // ==================== SHARED: LOAD ALL APPS + PAYMENT MAP ====================

    /**
     * Load all applications eagerly, then build a payment map keyed by applicationId.
     * All 3 transactional reports (1, 2, 3) start from this base.
     */
    private List<FacilityApplication> loadAllApps() {
        return applicationRepository.findAllForReport();
    }

    private Map<Integer, Payment> buildPaymentMap(List<FacilityApplication> apps) {
        Set<Integer> appIds = apps.stream()
                .map(FacilityApplication::getApplicationId)
                .collect(Collectors.toSet());
        return paymentRepository.findAll().stream()
                .filter(p -> appIds.contains(p.getApplication().getApplicationId()))
                .collect(Collectors.toMap(
                        p -> p.getApplication().getApplicationId(),
                        p -> p,
                        (a, b) -> a
                ));
    }

    // ==================== SHARED: COMMON FILTERS ====================

    private boolean matchEvent(FacilityApplication app, Integer eventId) {
        return eventId == null || Objects.equals(app.getEventFacility().getEvent().getEventId(), eventId);
    }

    private boolean matchFacility(FacilityApplication app, Integer facilityId) {
        return facilityId == null || Objects.equals(app.getEventFacility().getFacility().getFacilityId(), facilityId);
    }

    private boolean matchOwnerCategory(FacilityApplication app, String ownerCategory) {
        return ownerCategory == null || ownerCategory.isBlank()
                || app.getBusiness().getUser().getUserCategory().name().equalsIgnoreCase(ownerCategory);
    }

    private boolean matchApplicationStatus(FacilityApplication app, String status) {
        return status == null || status.isBlank()
                || app.getApplicationStatus().name().equalsIgnoreCase(status);
    }

    private boolean matchApplicationDateRange(FacilityApplication app, LocalDate start, LocalDate end) {
        if (start == null && end == null) return true;
        LocalDate d = app.getApplicationCreatedAt().toLocalDate();
        if (start != null && d.isBefore(start)) return false;
        if (end   != null && d.isAfter(end))    return false;
        return true;
    }

    private boolean matchPaymentStatus(Payment payment, String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) return true;
        if (payment == null) return false;
        return payment.getPaymentStatus().name().equalsIgnoreCase(paymentStatus);
    }

    private boolean matchPaymentDateRange(Payment payment, LocalDate start, LocalDate end) {
        if (start == null && end == null) return true;
        if (payment == null) return false;
        LocalDate d = payment.getPaymentCreatedAt().toLocalDate();
        if (start != null && d.isBefore(start)) return false;
        if (end   != null && d.isAfter(end))    return false;
        return true;
    }

    private boolean matchEventDateRange(Event event, LocalDate start, LocalDate end) {
        if (start == null && end == null) return true;
        LocalDate d = event.getEventStartDate();
        if (start != null && d.isBefore(start)) return false;
        if (end   != null && d.isAfter(end))    return false;
        return true;
    }

    // ==================== REPORT 1: FACILITY RENTAL ====================

    @Transactional(readOnly = true)
    public ReportResponse<FacilityRentalSummary, FacilityRentalRow> getFacilityRentalReport(
            Integer eventId, Integer facilityId, String ownerCategory,
            String applicationStatus, String paymentStatus,
            LocalDate startDate, LocalDate endDate) {

        List<FacilityApplication> apps = loadAllApps();
        Map<Integer, Payment> paymentMap = buildPaymentMap(apps);

        // Apply all filters
        List<FacilityApplication> filtered = apps.stream()
                .filter(a -> matchEvent(a, eventId))
                .filter(a -> matchFacility(a, facilityId))
                .filter(a -> matchOwnerCategory(a, ownerCategory))
                .filter(a -> matchApplicationStatus(a, applicationStatus))
                .filter(a -> matchApplicationDateRange(a, startDate, endDate))
                .collect(Collectors.toList());

        // Payment status filter — applied after joining with payment
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            filtered = filtered.stream()
                    .filter(a -> matchPaymentStatus(paymentMap.get(a.getApplicationId()), paymentStatus))
                    .collect(Collectors.toList());
        }

        // Build rows
        List<FacilityRentalRow> rows = filtered.stream()
                .map(a -> {
                    Payment p = paymentMap.get(a.getApplicationId());
                    EventFacility ef = a.getEventFacility();
                    Event event = ef.getEvent();
                    Facility facility = ef.getFacility();
                    Business business = a.getBusiness();
                    User owner = business.getUser();

                    FacilityRentalRow row = new FacilityRentalRow();
                    row.setApplicationId(a.getApplicationId());
                    row.setEventName(event.getEventName());
                    row.setEventVenue(event.getEventVenue());
                    row.setEventStatus(event.getEventStatus());
                    row.setFacilityName(facility.getFacilityName());
                    row.setFacilityType(facility.getFacilityType());
                    row.setBusinessId(business.getBusinessId());
                    row.setBusinessName(business.getBusinessName());
                    row.setOwnerId(owner.getUserId());
                    row.setOwnerName(owner.getUserName());
                    row.setOwnerCategory(owner.getUserCategory().name());
                    row.setApplicationStatus(a.getApplicationStatus().name());
                    row.setApplicationCreatedAt(a.getApplicationCreatedAt());
                    if (p != null) {
                        row.setPaymentStatus(p.getPaymentStatus().name());
                        row.setPaymentAmount(p.getPaymentAmount());
                    }
                    return row;
                })
                .collect(Collectors.toList());

        // Build summary
        Map<Integer, Payment> filteredPaymentMap = filtered.stream()
                .filter(a -> paymentMap.containsKey(a.getApplicationId()))
                .collect(Collectors.toMap(
                        FacilityApplication::getApplicationId,
                        a -> paymentMap.get(a.getApplicationId()),
                        (x, y) -> x
                ));

        int total      = filtered.size();
        int approved   = count(filtered, FacilityApplication.ApplicationStatus.APPROVED);
        int rejected   = count(filtered, FacilityApplication.ApplicationStatus.REJECTED);
        int pending    = count(filtered, FacilityApplication.ApplicationStatus.PENDING);
        int cancelled  = count(filtered, FacilityApplication.ApplicationStatus.CANCELLED);
        int businesses = (int) filtered.stream().map(a -> a.getBusiness().getBusinessId()).distinct().count();

        List<Payment> payments = new ArrayList<>(filteredPaymentMap.values());
        int paidCount = countPayments(payments, Payment.PaymentStatus.PAID);
        BigDecimal revenue = sumPayments(payments, Payment.PaymentStatus.PAID);
        BigDecimal unpaid  = sumPayments(payments, Payment.PaymentStatus.UNPAID);
        int collectionRate = approved > 0 ? (paidCount * 100 / approved) : 0;

        FacilityRentalSummary summary = new FacilityRentalSummary(
                total, approved, rejected, pending, cancelled,
                businesses, paidCount, revenue, unpaid, collectionRate);

        return new ReportResponse<>(summary, rows);
    }

    // ==================== REPORT 2: REVENUE & PAYMENT ====================

    @Transactional(readOnly = true)
    public ReportResponse<RevenueSummary, RevenueRow> getRevenueReport(
            Integer eventId, String ownerCategory, String paymentStatus,
            LocalDate startDate, LocalDate endDate) {

        List<FacilityApplication> apps = loadAllApps();

        // Start from APPROVED applications only (they're the ones that have payment records)
        List<FacilityApplication> approved = apps.stream()
                .filter(a -> a.getApplicationStatus() == FacilityApplication.ApplicationStatus.APPROVED)
                .filter(a -> matchEvent(a, eventId))
                .filter(a -> matchOwnerCategory(a, ownerCategory))
                .collect(Collectors.toList());

        Map<Integer, Payment> paymentMap = buildPaymentMap(approved);

        // Filter by payment date range and payment status
        List<Map.Entry<FacilityApplication, Payment>> pairs = approved.stream()
                .filter(a -> paymentMap.containsKey(a.getApplicationId()))
                .map(a -> Map.entry(a, paymentMap.get(a.getApplicationId())))
                .filter(e -> matchPaymentDateRange(e.getValue(), startDate, endDate))
                .filter(e -> matchPaymentStatus(e.getValue(), paymentStatus))
                .collect(Collectors.toList());

        // Build rows
        List<RevenueRow> rows = pairs.stream()
                .map(e -> {
                    FacilityApplication a = e.getKey();
                    Payment p = e.getValue();
                    RevenueRow row = new RevenueRow();
                    row.setApplicationId(a.getApplicationId());
                    row.setEventName(a.getEventFacility().getEvent().getEventName());
                    row.setBusinessName(a.getBusiness().getBusinessName());
                    row.setOwnerName(a.getBusiness().getUser().getUserName());
                    row.setOwnerCategory(a.getBusiness().getUser().getUserCategory().name());
                    row.setAmountBilled(p.getPaymentAmount());
                    row.setPaymentStatus(p.getPaymentStatus().name());
                    row.setPaymentCreatedAt(p.getPaymentCreatedAt());
                    return row;
                })
                .collect(Collectors.toList());

        // Build summary from all payment records (before paymentStatus filter for full picture)
        List<Payment> allPayments = pairs.stream().map(Map.Entry::getValue).collect(Collectors.toList());
        BigDecimal billed      = allPayments.stream().map(Payment::getPaymentAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal collected   = sumPayments(allPayments, Payment.PaymentStatus.PAID);
        BigDecimal outstanding = sumPayments(allPayments, Payment.PaymentStatus.UNPAID);
        BigDecimal failed      = sumPayments(allPayments, Payment.PaymentStatus.FAILED);

        int paidCount   = countPayments(allPayments, Payment.PaymentStatus.PAID);
        int unpaidCount = countPayments(allPayments, Payment.PaymentStatus.UNPAID);
        int failedCount = countPayments(allPayments, Payment.PaymentStatus.FAILED);
        int total       = allPayments.size();

        int collectionRate = (paidCount + unpaidCount) > 0
                ? (paidCount * 100 / (paidCount + unpaidCount)) : 0;

        // Avg payment per distinct business that paid
        long paidBusinessCount = pairs.stream()
                .filter(e -> e.getValue().getPaymentStatus() == Payment.PaymentStatus.PAID)
                .map(e -> e.getKey().getBusiness().getBusinessId())
                .distinct().count();
        BigDecimal avg = paidBusinessCount > 0
                ? collected.divide(BigDecimal.valueOf(paidBusinessCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        RevenueSummary summary = new RevenueSummary(
                billed, collected, outstanding, failed,
                collectionRate, avg, total, paidCount, unpaidCount, failedCount);

        return new ReportResponse<>(summary, rows);
    }

    // ==================== REPORT 3: BUSINESS OWNER ACTIVITY ====================

    @Transactional(readOnly = true)
    public ReportResponse<BusinessActivitySummary, BusinessActivityRow> getBusinessActivityReport(
            String ownerCategory, Integer eventId, LocalDate startDate, LocalDate endDate) {

        List<FacilityApplication> apps = loadAllApps();
        Map<Integer, Payment> paymentMap = buildPaymentMap(apps);

        // Apply filters
        List<FacilityApplication> filtered = apps.stream()
                .filter(a -> matchOwnerCategory(a, ownerCategory))
                .filter(a -> matchEvent(a, eventId))
                .filter(a -> matchApplicationDateRange(a, startDate, endDate))
                .collect(Collectors.toList());

        // Group by business
        Map<Long, List<FacilityApplication>> byBusiness = filtered.stream()
                .collect(Collectors.groupingBy(a -> a.getBusiness().getBusinessId()));

        List<BusinessActivityRow> rows = byBusiness.entrySet().stream()
                .map(entry -> {
                    Long businessId = entry.getKey();
                    List<FacilityApplication> bApps = entry.getValue();
                    FacilityApplication sample = bApps.get(0);
                    Business business = sample.getBusiness();
                    User owner = business.getUser();

                    int totalApplied   = bApps.size();
                    int totalApproved  = count(bApps, FacilityApplication.ApplicationStatus.APPROVED);
                    int totalRejected  = count(bApps, FacilityApplication.ApplicationStatus.REJECTED);
                    int totalCancelled = count(bApps, FacilityApplication.ApplicationStatus.CANCELLED);

                    // Get payments for this business's approved applications
                    List<Payment> bPayments = bApps.stream()
                            .filter(a -> paymentMap.containsKey(a.getApplicationId()))
                            .map(a -> paymentMap.get(a.getApplicationId()))
                            .collect(Collectors.toList());
                    int totalPaid = countPayments(bPayments, Payment.PaymentStatus.PAID);
                    BigDecimal revenuePaid = sumPayments(bPayments, Payment.PaymentStatus.PAID);

                    int approvalRate     = totalApplied > 0 ? (totalApproved  * 100 / totalApplied)  : 0;
                    int cancellationRate = totalApplied > 0 ? (totalCancelled * 100 / totalApplied)  : 0;
                    int paymentRate      = totalApproved > 0 ? (totalPaid     * 100 / totalApproved) : 0;

                    return new BusinessActivityRow(
                            businessId,
                            business.getBusinessName(),
                            owner.getUserName(),
                            owner.getUserCategory().name(),
                            totalApplied, totalApproved, totalRejected, totalCancelled,
                            totalPaid, approvalRate, cancellationRate, paymentRate, revenuePaid);
                })
                .sorted(Comparator.comparingInt(BusinessActivityRow::getTotalApplied).reversed())
                .collect(Collectors.toList());

        // Build summary
        int totalBusinesses    = rows.size();
        int totalApplications  = rows.stream().mapToInt(BusinessActivityRow::getTotalApplied).sum();
        int totalApproved      = rows.stream().mapToInt(BusinessActivityRow::getTotalApproved).sum();
        int overallApprovalRate = totalApplications > 0 ? (totalApproved * 100 / totalApplications) : 0;

        String mostActiveOwner = rows.isEmpty() ? "N/A" : rows.get(0).getBusinessName();
        int mostActiveCount    = rows.isEmpty() ? 0 : rows.get(0).getTotalApplied();

        String highestCancellation = rows.stream()
                .max(Comparator.comparingInt(BusinessActivityRow::getCancellationRate))
                .map(BusinessActivityRow::getBusinessName)
                .orElse("N/A");

        BusinessActivitySummary summary = new BusinessActivitySummary(
                totalBusinesses, totalApplications, overallApprovalRate,
                mostActiveOwner, mostActiveCount, highestCancellation);

        return new ReportResponse<>(summary, rows);
    }

    // ==================== REPORT 4: EVENT PERFORMANCE ====================

    @Transactional(readOnly = true)
    public ReportResponse<EventPerformanceSummary, EventPerformanceRow> getEventPerformanceReport(
            String eventStatus, LocalDate startDate, LocalDate endDate) {

        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> eventStatus == null || eventStatus.isBlank()
                        || e.getEventStatus().equalsIgnoreCase(eventStatus))
                .filter(e -> matchEventDateRange(e, startDate, endDate))
                .collect(Collectors.toList());

        // Load all event facilities (slots info) and all applications
        List<EventFacility> allEFs = eventFacilityRepository.findAll();
        List<FacilityApplication> allApps = loadAllApps();
        Map<Integer, Payment> paymentMap = buildPaymentMap(allApps);

        // Group by event
        Map<Integer, List<EventFacility>> efsByEvent = allEFs.stream()
                .collect(Collectors.groupingBy(ef -> ef.getEvent().getEventId()));
        Map<Integer, List<FacilityApplication>> appsByEvent = allApps.stream()
                .collect(Collectors.groupingBy(a -> a.getEventFacility().getEvent().getEventId()));

        List<EventPerformanceRow> rows = events.stream()
                .map(event -> {
                    Integer eventId = event.getEventId();
                    List<EventFacility> efs = efsByEvent.getOrDefault(eventId, Collections.emptyList());
                    List<FacilityApplication> eApps = appsByEvent.getOrDefault(eventId, Collections.emptyList());

                    int facilitiesOffered = efs.size();
                    // originalQuantityTotal is set on @PrePersist — fall back to quantityFacilityAvailable if null
                    int slotsAvailable = efs.stream()
                            .mapToInt(ef -> ef.getOriginalQuantityTotal() != null
                                    ? ef.getOriginalQuantityTotal()
                                    : (ef.getQuantityFacilityAvailable() != null ? ef.getQuantityFacilityAvailable() : 0))
                            .sum();
                    int currentRemaining = efs.stream()
                            .mapToInt(ef -> ef.getQuantityFacilityAvailable() != null ? ef.getQuantityFacilityAvailable() : 0)
                            .sum();
                    int slotsFilled = Math.max(0, slotsAvailable - currentRemaining);
                    int fillRate    = slotsAvailable > 0 ? (slotsFilled * 100 / slotsAvailable) : 0;

                    int totalApps     = eApps.size();
                    int totalApproved = count(eApps, FacilityApplication.ApplicationStatus.APPROVED);
                    int totalRejected = count(eApps, FacilityApplication.ApplicationStatus.REJECTED);

                    List<Payment> ePayments = eApps.stream()
                            .filter(a -> paymentMap.containsKey(a.getApplicationId()))
                            .map(a -> paymentMap.get(a.getApplicationId()))
                            .collect(Collectors.toList());
                    BigDecimal revenue = sumPayments(ePayments, Payment.PaymentStatus.PAID);

                    return new EventPerformanceRow(
                            eventId, event.getEventName(), event.getEventVenue(), event.getEventStatus(),
                            event.getEventStartDate(), event.getEventEndDate(),
                            facilitiesOffered, slotsAvailable, slotsFilled, fillRate,
                            totalApps, totalApproved, totalRejected, revenue);
                })
                .sorted(Comparator.comparing(EventPerformanceRow::getEventStartDate).reversed())
                .collect(Collectors.toList());

        // Summary
        int totalEvents   = rows.size();
        int fullyBooked   = (int) rows.stream().filter(r -> r.getFillRate() >= 100).count();
        int totalApps     = rows.stream().mapToInt(EventPerformanceRow::getTotalApplications).sum();
        BigDecimal totalRevenue = rows.stream().map(EventPerformanceRow::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        int avgApps       = totalEvents > 0 ? (totalApps / totalEvents) : 0;
        String best       = rows.stream()
                .max(Comparator.comparing(EventPerformanceRow::getTotalRevenue))
                .map(EventPerformanceRow::getEventName)
                .orElse("N/A");

        EventPerformanceSummary summary = new EventPerformanceSummary(
                totalEvents, fullyBooked, totalApps, totalRevenue, avgApps, best);

        return new ReportResponse<>(summary, rows);
    }

    // ==================== REPORT 5: FACILITY UTILISATION ====================

    @Transactional(readOnly = true)
    public ReportResponse<FacilityUtilisationSummary, FacilityUtilisationRow> getFacilityUtilisationReport(
            String facilityType, Integer eventId, LocalDate startDate, LocalDate endDate) {

        List<EventFacility> allEFs = eventFacilityRepository.findAll().stream()
                .filter(ef -> facilityType == null || facilityType.isBlank()
                        || ef.getFacility().getFacilityType().equalsIgnoreCase(facilityType))
                .filter(ef -> eventId == null || Objects.equals(ef.getEvent().getEventId(), eventId))
                .filter(ef -> matchEventDateRange(ef.getEvent(), startDate, endDate))
                .collect(Collectors.toList());

        List<FacilityApplication> allApps = loadAllApps();
        Map<Integer, Payment> paymentMap = buildPaymentMap(allApps);

        // Group by facility template (facilityId)
        Map<Integer, List<EventFacility>> efsByFacility = allEFs.stream()
                .collect(Collectors.groupingBy(ef -> ef.getFacility().getFacilityId()));

        // Group applications by eventFacilityId for efficient lookup
        Map<Integer, List<FacilityApplication>> appsByEF = allApps.stream()
                .collect(Collectors.groupingBy(a -> a.getEventFacility().getEventFacilityId()));

        List<FacilityUtilisationRow> rows = efsByFacility.entrySet().stream()
                .map(entry -> {
                    Integer facilityId2 = entry.getKey();
                    List<EventFacility> fEFs = entry.getValue();
                    Facility facility = fEFs.get(0).getFacility();

                    int timesOffered    = fEFs.size();
                    int slotsOffered    = fEFs.stream()
                            .mapToInt(ef -> ef.getOriginalQuantityTotal() != null
                                    ? ef.getOriginalQuantityTotal()
                                    : (ef.getQuantityFacilityAvailable() != null ? ef.getQuantityFacilityAvailable() : 0))
                            .sum();
                    int currentRemaining = fEFs.stream()
                            .mapToInt(ef -> ef.getQuantityFacilityAvailable() != null ? ef.getQuantityFacilityAvailable() : 0)
                            .sum();
                    int slotsFilled = Math.max(0, slotsOffered - currentRemaining);
                    int fillRate    = slotsOffered > 0 ? (slotsFilled * 100 / slotsOffered) : 0;

                    // Collect all applications for this facility across events
                    List<FacilityApplication> fApps = fEFs.stream()
                            .flatMap(ef -> appsByEF.getOrDefault(ef.getEventFacilityId(), Collections.emptyList()).stream())
                            .collect(Collectors.toList());

                    int totalApps     = fApps.size();
                    int totalApproved = count(fApps, FacilityApplication.ApplicationStatus.APPROVED);

                    List<Payment> fPayments = fApps.stream()
                            .filter(a -> paymentMap.containsKey(a.getApplicationId()))
                            .map(a -> paymentMap.get(a.getApplicationId()))
                            .collect(Collectors.toList());
                    BigDecimal revenue = sumPayments(fPayments, Payment.PaymentStatus.PAID);

                    return new FacilityUtilisationRow(
                            facilityId2, facility.getFacilityName(), facility.getFacilityType(),
                            facility.getFacilitySize(), timesOffered, slotsOffered, slotsFilled,
                            fillRate, totalApps, totalApproved, revenue);
                })
                .sorted(Comparator.comparingInt(FacilityUtilisationRow::getTotalApplications).reversed())
                .collect(Collectors.toList());

        // Summary
        int totalFacilities = rows.size();
        int totalSlotsOffered = rows.stream().mapToInt(FacilityUtilisationRow::getTotalSlotsOffered).sum();
        int totalSlotsFilled  = rows.stream().mapToInt(FacilityUtilisationRow::getTotalSlotsFilled).sum();
        int overallFillRate   = totalSlotsOffered > 0 ? (totalSlotsFilled * 100 / totalSlotsOffered) : 0;

        String mostDemanded = rows.isEmpty() ? "N/A" : rows.get(0).getFacilityName();
        int mostDemandedCount = rows.isEmpty() ? 0 : rows.get(0).getTotalApplications();

        String highestFill = rows.stream()
                .max(Comparator.comparingInt(FacilityUtilisationRow::getFillRate))
                .map(FacilityUtilisationRow::getFacilityName).orElse("N/A");
        String lowestFill = rows.stream()
                .min(Comparator.comparingInt(FacilityUtilisationRow::getFillRate))
                .map(FacilityUtilisationRow::getFacilityName).orElse("N/A");

        FacilityUtilisationSummary summary = new FacilityUtilisationSummary(
                totalFacilities, overallFillRate, mostDemanded, mostDemandedCount, highestFill, lowestFill);

        return new ReportResponse<>(summary, rows);
    }

    // ==================== PRIVATE HELPERS ====================

    private int count(List<FacilityApplication> apps, FacilityApplication.ApplicationStatus status) {
        return (int) apps.stream().filter(a -> a.getApplicationStatus() == status).count();
    }

    private int countPayments(List<Payment> payments, Payment.PaymentStatus status) {
        return (int) payments.stream().filter(p -> p.getPaymentStatus() == status).count();
    }

    private BigDecimal sumPayments(List<Payment> payments, Payment.PaymentStatus status) {
        return payments.stream()
                .filter(p -> p.getPaymentStatus() == status)
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}