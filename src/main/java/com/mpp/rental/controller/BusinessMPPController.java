package com.mpp.rental.controller;

import com.mpp.rental.dto.ApiResponse;
import com.mpp.rental.dto.BusinessMPPResponse;
import com.mpp.rental.dto.BusinessSearchFilterRequest;
import com.mpp.rental.dto.UpdateBusinessStatusRequest;
import com.mpp.rental.service.BusinessMPPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for MPP Business Management
 * Handles business viewing, search, filter, and status management
 */
@RestController
@RequestMapping("/api/mpp/businesses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MPP')") // All endpoints require MPP role
public class BusinessMPPController {

    private final BusinessMPPService businessMPPService;

    /**
     * Get all businesses with search and filter
     * GET /api/mpp/businesses
     *
     * Query Parameters (all optional):
     * - searchQuery: Search by business name, SSM number, or owner name
     * - businessCategory: Filter by category (Food, Clothing, Services, etc.)
     * - businessStatus: Filter by status (ACTIVE, BLOCKED)
     * - ownerCategory: Filter by owner type (STUDENT, NON_STUDENT)
     * - startDate: Filter by registration date (from)
     * - endDate: Filter by registration date (to)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BusinessMPPResponse>>> getAllBusinesses(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String businessCategory,
            @RequestParam(required = false) String businessStatus,
            @RequestParam(required = false) String ownerCategory,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Build filter request
        BusinessSearchFilterRequest filterRequest = BusinessSearchFilterRequest.builder()
                .searchQuery(searchQuery)
                .businessCategory(businessCategory)
                .businessStatus(businessStatus)
                .ownerCategory(ownerCategory)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        List<BusinessMPPResponse> businesses = businessMPPService.getAllBusinessesWithFilters(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Businesses retrieved successfully",
                        businesses
                )
        );
    }

    /**
     * Get single business details with owner information
     * GET /api/mpp/businesses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessMPPResponse>> getBusinessDetails(@PathVariable Long id) {
        BusinessMPPResponse business = businessMPPService.getBusinessDetailForMPP(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business details retrieved successfully",
                        business
                )
        );
    }

    /**
     * Block business
     * PUT /api/mpp/businesses/{id}/block
     *
     * Request Body:
     * {
     *   "businessStatus": "BLOCKED",
     *   "reason": "Violation of terms" (optional)
     * }
     */
    @PutMapping("/{id}/block")
    public ResponseEntity<ApiResponse<BusinessMPPResponse>> blockBusiness(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBusinessStatusRequest request
    ) {
        BusinessMPPResponse business = businessMPPService.blockBusiness(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business blocked successfully",
                        business
                )
        );
    }

    /**
     * Activate business
     * PUT /api/mpp/businesses/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<BusinessMPPResponse>> activateBusiness(@PathVariable Long id) {
        BusinessMPPResponse business = businessMPPService.activateBusiness(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business activated successfully",
                        business
                )
        );
    }

    /**
     * Get business statistics
     * GET /api/mpp/businesses/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<BusinessMPPService.BusinessStatistics>> getStatistics() {
        BusinessMPPService.BusinessStatistics stats = businessMPPService.getBusinessStatistics();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Statistics retrieved successfully",
                        stats
                )
        );
    }

    /**
     * Get businesses by category
     * GET /api/mpp/businesses/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<BusinessMPPResponse>>> getBusinessesByCategory(
            @PathVariable String category
    ) {
        List<BusinessMPPResponse> businesses = businessMPPService.getBusinessesByCategory(category);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Businesses retrieved by category successfully",
                        businesses
                )
        );
    }

    /**
     * Get businesses by status
     * GET /api/mpp/businesses/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<BusinessMPPResponse>>> getBusinessesByStatus(
            @PathVariable String status
    ) {
        List<BusinessMPPResponse> businesses = businessMPPService.getBusinessesByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Businesses retrieved by status successfully",
                        businesses
                )
        );
    }

    /**
     * Get businesses by owner category
     * GET /api/mpp/businesses/owner-category/{ownerCategory}
     */
    @GetMapping("/owner-category/{ownerCategory}")
    public ResponseEntity<ApiResponse<List<BusinessMPPResponse>>> getBusinessesByOwnerCategory(
            @PathVariable String ownerCategory
    ) {
        List<BusinessMPPResponse> businesses = businessMPPService.getBusinessesByOwnerCategory(ownerCategory);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Businesses retrieved by owner category successfully",
                        businesses
                )
        );
    }
}