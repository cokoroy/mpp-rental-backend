package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.BusinessException;
import com.mpp.rental.model.User;
import com.mpp.rental.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    // ==================== BUSINESS OWNER ENDPOINTS ====================

    /**
     * Create a new business
     * POST /api/business/create
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT') or hasRole('NON_STUDENT')")
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(@Valid @RequestBody CreateBusinessRequest request) {
        BusinessResponse response = businessService.createBusiness(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business created successfully", response));
    }

    /**
     * Get all businesses owned by current user
     * GET /api/business/my-businesses
     */
    @GetMapping("/my-businesses")
    @PreAuthorize("hasRole('STUDENT') or hasRole('NON_STUDENT')")
    public ResponseEntity<ApiResponse<List<BusinessResponse>>> getMyBusinesses() {
        List<BusinessResponse> businesses = businessService.getMyBusinesses();

        return ResponseEntity.ok(ApiResponse.success("Businesses retrieved successfully", businesses));
    }

    /**
     * Get single business by ID
     * GET /api/business/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('NON_STUDENT')")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(@PathVariable Long id) {
        BusinessResponse response = businessService.getBusinessById(id);

        return ResponseEntity.ok(ApiResponse.success("Business retrieved successfully", response));
    }

    /**
     * Update business details
     * PUT /api/business/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('NON_STUDENT')")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBusinessRequest request) {

        BusinessResponse response = businessService.updateBusiness(id, request);

        return ResponseEntity.ok(ApiResponse.success("Business updated successfully", response));
    }

    /**
     * Delete business
     * DELETE /api/business/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('NON_STUDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);

        return ResponseEntity.ok(ApiResponse.success("Business deleted successfully", null));
    }

    /**
     * Upload SSM document
     * POST /api/business/{id}/upload-ssm
     */
    @PostMapping(value = "/{id}/upload-ssm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT') or hasRole('NON_STUDENT')")
    public ResponseEntity<ApiResponse<BusinessResponse>> uploadSsmDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        BusinessResponse response = businessService.uploadSsmDocument(id, file);

        return ResponseEntity.ok(ApiResponse.success("SSM document uploaded successfully", response));
    }

    @GetMapping("/{id}/ssm-document")
    @PreAuthorize("hasAnyRole('STUDENT', 'NON_STUDENT', 'MPP')")
    public ResponseEntity<Resource> downloadSSMDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            // Get the resource from service
            Resource resource = businessService.downloadSsmDocument(id, currentUser);


            // Get file path for content type detection
            String filename = resource.getFilename();
            String contentType = "application/octet-stream";

            if (filename != null) {
                if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.endsWith(".png")) {
                    contentType = "image/png";
                }
            }

            // Return file as downloadable resource
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Error downloading SSM document: " + e.getMessage());
        }
    }

    // ==================== MPP ENDPOINTS ====================

    /**
     * Get all businesses (MPP only)
     * GET /api/business/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<BusinessResponse>>> getAllBusinesses() {
        List<BusinessResponse> businesses = businessService.getAllBusinesses();

        return ResponseEntity.ok(ApiResponse.success("All businesses retrieved successfully", businesses));
    }

    /**
     * Get all businesses by owner ID (MPP only)
     * GET /api/business/admin/owner/{userId}
     */
    @GetMapping("/admin/owner/{userId}")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<List<BusinessResponse>>> getBusinessesByOwnerId(@PathVariable Long userId) {
        List<BusinessResponse> businesses = businessService.getBusinessesByOwnerId(userId);

        return ResponseEntity.ok(ApiResponse.success("Businesses retrieved successfully", businesses));
    }

    /**
     * Get business by ID (MPP only - admin view)
     * GET /api/business/admin/{id}
     */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessByIdAdmin(@PathVariable Long id) {
        BusinessResponse response = businessService.getBusinessByIdAdmin(id);

        return ResponseEntity.ok(ApiResponse.success("Business retrieved successfully", response));
    }

    /**
     * Block/Activate business owner (MPP only)
     * PUT /api/business/admin/owner/{userId}/status
     */
    @PutMapping("/admin/owner/{userId}/status")
    @PreAuthorize("hasRole('MPP')")
    public ResponseEntity<ApiResponse<Void>> updateBusinessOwnerStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateBusinessStatusRequest request) {

        businessService.updateBusinessOwnerStatus(userId, request.getBusinessStatus());

        String message = "BLOCKED".equals(request.getBusinessStatus())
                ? "Business owner blocked successfully"
                : "Business owner activated successfully";

        return ResponseEntity.ok(ApiResponse.success(message, null));
    }
}