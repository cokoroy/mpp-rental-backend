package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.exception.ApplicationException;
import com.mpp.rental.service.FacilityApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bo/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FacilityApplicationController {

    private final FacilityApplicationService facilityApplicationService;

    /**
     * Submit facility applications (one per selected facility)
     * POST /api/bo/applications
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<FacilityApplicationResponse>>> submitApplications(
            @Valid @RequestBody CreateApplicationRequest request) {
        try {
            List<FacilityApplicationResponse> responses = facilityApplicationService.submitApplications(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Application submitted successfully", responses));
        } catch (ApplicationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get all applications for the logged-in Business Owner
     * GET /api/bo/applications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FacilityApplicationResponse>>> getMyApplications() {
        try {
            List<FacilityApplicationResponse> responses = facilityApplicationService.getMyApplications();
            return ResponseEntity.ok(new ApiResponse<>(true, "Applications retrieved successfully", responses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Cancel a PENDING application (soft cancel — sets status to CANCELLED)
     * DELETE /api/bo/applications/{id}/cancel
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelApplication(@PathVariable Integer id) {
        try {
            facilityApplicationService.cancelApplication(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Application cancelled successfully", null));
        } catch (ApplicationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Delete a REJECTED or CANCELLED application (hard delete)
     * DELETE /api/bo/applications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(@PathVariable Integer id) {
        try {
            facilityApplicationService.deleteApplication(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Application deleted successfully", null));
        } catch (ApplicationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}