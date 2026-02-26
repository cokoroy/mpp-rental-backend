package com.mpp.rental.service;

import com.mpp.rental.dto.BusinessMPPResponse;
import com.mpp.rental.dto.BusinessSearchFilterRequest;
import com.mpp.rental.dto.UpdateBusinessStatusRequest;
import com.mpp.rental.exception.BusinessException;
import com.mpp.rental.exception.ResourceNotFoundException;
import com.mpp.rental.model.Business;
import com.mpp.rental.model.User;
import com.mpp.rental.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for MPP Business Management Operations
 * Handles business viewing, search, filter, and status management for MPP administrators
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessMPPService {

    private final BusinessRepository businessRepository;

    // ==================== MPP BUSINESS MANAGEMENT OPERATIONS ====================

    /**
     * Get all businesses with search and filter
     * MPP can search by: business name, SSM number, owner name
     * MPP can filter by: category, status, owner category, date range
     */
    public List<BusinessMPPResponse> getAllBusinessesWithFilters(BusinessSearchFilterRequest filterRequest) {
        log.info("MPP fetching businesses with filters: {}", filterRequest);

        List<Business> businesses;

        // If no filters provided, get all businesses
        if (isFilterEmpty(filterRequest)) {
            businesses = businessRepository.findAllWithOwnerInfo();
            log.info("No filters provided, fetching all {} businesses", businesses.size());
        } else {
            // Apply filters
            businesses = businessRepository.findAllWithFilters(
                    filterRequest.getSearchQuery(),
                    filterRequest.getBusinessCategory(),
                    filterRequest.getBusinessStatus(),
                    filterRequest.getOwnerCategory(),
                    filterRequest.getStartDate(),
                    filterRequest.getEndDate()
            );
            log.info("Filters applied, found {} businesses", businesses.size());
        }

        return businesses.stream()
                .map(this::mapToBusinessMPPResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get single business details with owner information (MPP view)
     */
    public BusinessMPPResponse getBusinessDetailForMPP(Long businessId) {
        log.info("MPP fetching business details for ID: {}", businessId);

        Business business = businessRepository.findByIdWithOwner(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        return mapToBusinessMPPResponse(business);
    }

    /**
     * Block business
     * - Updates status to BLOCKED
     * - Rejects pending applications (TODO: implement when FacilityApplication exists)
     * - Cannot block if business has approved applications
     */
    @Transactional
    public BusinessMPPResponse blockBusiness(Long businessId, UpdateBusinessStatusRequest request) {
        log.info("MPP attempting to block business ID: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        // Check if already blocked
        if ("BLOCKED".equals(business.getBusinessStatus())) {
            throw new BusinessException("Business is already blocked");
        }

        // TODO: Check if business has approved applications
        // This will be implemented when FacilityApplication entity is created
        // if (businessRepository.hasApprovedApplications(businessId)) {
        //     throw new BusinessException(
        //         "Cannot block business with approved applications. " +
        //         "Please wait for all approved applications to complete."
        //     );
        // }

        // Update business status to BLOCKED
        business.setBusinessStatus("BLOCKED");
        Business blockedBusiness = businessRepository.save(business);

        // TODO: Auto-reject all pending applications
        // This will be implemented when FacilityApplication entity is created
        // facilityApplicationRepository.rejectPendingApplicationsByBusiness(
        //     businessId,
        //     "Business has been blocked by administrator"
        // );

        log.info("Business blocked successfully: {} (ID: {}). Reason: {}",
                business.getBusinessName(), businessId, request.getReason());

        return mapToBusinessMPPResponse(blockedBusiness);
    }

    /**
     * Activate business
     * - Updates status to ACTIVE
     */
    @Transactional
    public BusinessMPPResponse activateBusiness(Long businessId) {
        log.info("MPP attempting to activate business ID: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        // Check if already active
        if ("ACTIVE".equals(business.getBusinessStatus())) {
            throw new BusinessException("Business is already active");
        }

        // Update business status to ACTIVE
        business.setBusinessStatus("ACTIVE");
        Business activatedBusiness = businessRepository.save(business);

        log.info("Business activated successfully: {} (ID: {})",
                business.getBusinessName(), businessId);

        return mapToBusinessMPPResponse(activatedBusiness);
    }

    /**
     * Get business statistics for MPP dashboard
     */
    public BusinessStatistics getBusinessStatistics() {
        long totalBusinesses = businessRepository.count();
        long activeBusinesses = businessRepository.countActiveBusinesses();
        long blockedBusinesses = businessRepository.countBlockedBusinesses();

        log.info("MPP fetching business statistics - Total: {}, Active: {}, Blocked: {}",
                totalBusinesses, activeBusinesses, blockedBusinesses);

        return BusinessStatistics.builder()
                .totalBusinesses(totalBusinesses)
                .activeBusinesses(activeBusinesses)
                .blockedBusinesses(blockedBusinesses)
                .build();
    }

    /**
     * Get businesses by category
     */
    public List<BusinessMPPResponse> getBusinessesByCategory(String category) {
        log.info("MPP fetching businesses by category: {}", category);

        List<Business> businesses = businessRepository.findByBusinessCategory(category);

        return businesses.stream()
                .map(this::mapToBusinessMPPResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get businesses by status
     */
    public List<BusinessMPPResponse> getBusinessesByStatus(String status) {
        log.info("MPP fetching businesses by status: {}", status);

        List<Business> businesses = businessRepository.findByBusinessStatus(status);

        return businesses.stream()
                .map(this::mapToBusinessMPPResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get businesses by owner category (STUDENT or NON_STUDENT)
     * FIXED: Pass String to repository instead of enum
     */
    public List<BusinessMPPResponse> getBusinessesByOwnerCategory(String ownerCategory) {
        log.info("MPP fetching businesses by owner category: {}", ownerCategory);

        // Validate that the category is valid (throws exception if invalid)
        try {
            User.UserCategory.valueOf(ownerCategory.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid owner category: " + ownerCategory);
        }

        // Pass the string directly to repository (after validation)
        List<Business> businesses = businessRepository.findByOwnerCategory(ownerCategory.toUpperCase());

        return businesses.stream()
                .map(this::mapToBusinessMPPResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if filter request is empty (no filters applied)
     */
    private boolean isFilterEmpty(BusinessSearchFilterRequest request) {
        return (request.getSearchQuery() == null || request.getSearchQuery().trim().isEmpty()) &&
                request.getBusinessCategory() == null &&
                request.getBusinessStatus() == null &&
                request.getOwnerCategory() == null &&
                request.getStartDate() == null &&
                request.getEndDate() == null;
    }

    /**
     * Map Business entity to BusinessMPPResponse DTO
     * Includes owner information for MPP view
     */
    private BusinessMPPResponse mapToBusinessMPPResponse(Business business) {
        User owner = business.getUser();

        return BusinessMPPResponse.builder()
                // Business information
                .businessId(business.getBusinessId())
                .businessName(business.getBusinessName())
                .ssmNumber(business.getSsmNumber())
                .businessCategory(business.getBusinessCategory())
                .businessDesc(business.getBusinessDesc())
                .businessStatus(business.getBusinessStatus())
                .businessRegisteredAt(business.getBusinessRegisteredAt())
                .ssmDocument(business.getSsmDocument())
                // Owner information
                .ownerId(owner.getUserId())
                .ownerName(owner.getUserName())
                .ownerEmail(owner.getUserEmail())
                .ownerPhoneNumber(owner.getUserPhoneNumber())
                .ownerCategory(owner.getUserCategory().name())
                .ownerStatus(owner.getUserStatus().name())
                .build();
    }

    /**
     * Inner class for business statistics
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BusinessStatistics {
        private long totalBusinesses;
        private long activeBusinesses;
        private long blockedBusinesses;
    }
}