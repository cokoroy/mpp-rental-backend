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
}