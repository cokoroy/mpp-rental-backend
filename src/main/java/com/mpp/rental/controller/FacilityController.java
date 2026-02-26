package com.mpp.rental.controller;

import com.mpp.rental.dto.ApiResponse;
import com.mpp.rental.dto.CreateFacilityRequest;
import com.mpp.rental.dto.FacilityResponse;
import com.mpp.rental.dto.FacilitySearchFilterRequest;
import com.mpp.rental.dto.UpdateFacilityRequest;
import com.mpp.rental.service.FacilityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/mpp/facilities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FacilityController {

    private final FacilityService facilityService;

    /**
     * Create new facility
     * POST /api/mpp/facilities
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FacilityResponse>> createFacility(
            @RequestParam("facilityName") String facilityName,
            @RequestParam("facilitySize") String facilitySize,
            @RequestParam("facilityType") String facilityType,
            @RequestParam("facilityDesc") String facilityDesc,
            @RequestParam("usage") String usage,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam("facilityBaseStudentPrice") BigDecimal facilityBaseStudentPrice,
            @RequestParam("facilityBaseNonstudentPrice") BigDecimal facilityBaseNonstudentPrice,
            @RequestParam("facilityStatus") String facilityStatus,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            // Trim and validate all string fields
            facilityName = facilityName != null ? facilityName.trim() : "";
            facilitySize = facilitySize != null ? facilitySize.trim() : "";
            facilityType = facilityType != null ? facilityType.trim() : "";
            facilityDesc = facilityDesc != null ? facilityDesc.trim() : "";
            usage = usage != null ? usage.trim() : "";
            remark = remark != null ? remark.trim() : null;

            // Validate required fields
            if (facilityName.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility name is required", null));
            }
            if (facilitySize.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility size is required", null));
            }
            if (facilityType.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility type is required", null));
            }
            if (facilityDesc.isEmpty() || facilityDesc.length() < 10) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility description must be at least 10 characters", null));
            }
            if (usage.isEmpty() || usage.length() < 10) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility usage must be at least 10 characters", null));
            }

            // Create request object manually
            CreateFacilityRequest request = new CreateFacilityRequest();
            request.setFacilityName(facilityName);
            request.setFacilitySize(facilitySize);
            request.setFacilityType(facilityType);
            request.setFacilityDesc(facilityDesc);
            request.setUsage(usage);
            request.setRemark(remark);
            request.setFacilityBaseStudentPrice(facilityBaseStudentPrice);
            request.setFacilityBaseNonstudentPrice(facilityBaseNonstudentPrice);
            request.setFacilityStatus(facilityStatus);

            FacilityResponse response = facilityService.createFacility(request, image);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Facility created successfully", response));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to upload image: " + e.getMessage(), null));
        }
    }

    /**
     * Get all facilities with optional search and filters
     * GET /api/mpp/facilities
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getAllFacilities(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "all") String facilityType,
            @RequestParam(required = false, defaultValue = "all") String facilitySize,
            @RequestParam(required = false, defaultValue = "all") String facilityStatus) {

        FacilitySearchFilterRequest filter = new FacilitySearchFilterRequest(
                searchQuery, facilityType, facilitySize, facilityStatus
        );

        List<FacilityResponse> facilities = facilityService.getAllFacilities(filter);
        return ResponseEntity.ok(new ApiResponse<>(true, "Facilities retrieved successfully", facilities));
    }

    /**
     * Get facility by ID
     * GET /api/mpp/facilities/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityResponse>> getFacilityById(@PathVariable Integer id) {
        FacilityResponse response = facilityService.getFacilityById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Facility retrieved successfully", response));
    }

    /**
     * Update facility
     * PUT /api/mpp/facilities/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FacilityResponse>> updateFacility(
            @PathVariable Integer id,
            @RequestParam("facilityName") String facilityName,
            @RequestParam("facilitySize") String facilitySize,
            @RequestParam("facilityType") String facilityType,
            @RequestParam("facilityDesc") String facilityDesc,
            @RequestParam("usage") String usage,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam("facilityBaseStudentPrice") BigDecimal facilityBaseStudentPrice,
            @RequestParam("facilityBaseNonstudentPrice") BigDecimal facilityBaseNonstudentPrice,
            @RequestParam("facilityStatus") String facilityStatus,
            @RequestParam(value = "removeImage", required = false) String removeImage,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            // Trim and validate all string fields
            facilityName = facilityName != null ? facilityName.trim() : "";
            facilitySize = facilitySize != null ? facilitySize.trim() : "";
            facilityType = facilityType != null ? facilityType.trim() : "";
            facilityDesc = facilityDesc != null ? facilityDesc.trim() : "";
            usage = usage != null ? usage.trim() : "";
            remark = remark != null ? remark.trim() : null;

            // Validate required fields
            if (facilityName.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility name is required", null));
            }
            if (facilitySize.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility size is required", null));
            }
            if (facilityType.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility type is required", null));
            }
            if (facilityDesc.isEmpty() || facilityDesc.length() < 10) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility description must be at least 10 characters", null));
            }
            if (usage.isEmpty() || usage.length() < 10) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Facility usage must be at least 10 characters", null));
            }

            // Create request object manually
            UpdateFacilityRequest request = new UpdateFacilityRequest();
            request.setFacilityName(facilityName);
            request.setFacilitySize(facilitySize);
            request.setFacilityType(facilityType);
            request.setFacilityDesc(facilityDesc);
            request.setUsage(usage);
            request.setRemark(remark);
            request.setFacilityBaseStudentPrice(facilityBaseStudentPrice);
            request.setFacilityBaseNonstudentPrice(facilityBaseNonstudentPrice);
            request.setFacilityStatus(facilityStatus);

            // Check if image should be removed
            boolean shouldRemoveImage = "true".equalsIgnoreCase(removeImage);

            FacilityResponse response = facilityService.updateFacility(id, request, image, shouldRemoveImage);
            return ResponseEntity.ok(new ApiResponse<>(true, "Facility updated successfully", response));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to upload image: " + e.getMessage(), null));
        }
    }

    /**
     * Delete facility (soft delete)
     * DELETE /api/mpp/facilities/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFacility(@PathVariable Integer id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Facility deleted successfully", null));
    }

    /**
     * Toggle facility status (activate/deactivate)
     * PATCH /api/mpp/facilities/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<FacilityResponse>> toggleFacilityStatus(@PathVariable Integer id) {
        FacilityResponse response = facilityService.toggleFacilityStatus(id);
        String message = response.getFacilityStatus().equals("active")
                ? "Facility activated successfully"
                : "Facility deactivated successfully";
        return ResponseEntity.ok(new ApiResponse<>(true, message, response));
    }

    /**
     * Get only active facilities
     * GET /api/mpp/facilities/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getActiveFacilities() {
        List<FacilityResponse> facilities = facilityService.getActiveFacilities();
        return ResponseEntity.ok(new ApiResponse<>(true, "Active facilities retrieved successfully", facilities));
    }
}