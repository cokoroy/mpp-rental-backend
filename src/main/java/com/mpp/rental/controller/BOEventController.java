package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.service.FacilityApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bo/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BOEventController {

    private final FacilityApplicationService facilityApplicationService;

    /**
     * Get all events for Business Owner
     * GET /api/bo/events
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsForBO(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "all") String eventStatus) {

        List<EventResponse> events = facilityApplicationService.getEventsForBO(searchQuery, eventStatus);
        return ResponseEntity.ok(new ApiResponse<>(true, "Events retrieved successfully", events));
    }

    /**
     * Get event with facilities for Business Owner (includes applicable price & quota info)
     * GET /api/bo/events/{id}/with-facilities
     */
    @GetMapping("/{id}/with-facilities")
    public ResponseEntity<ApiResponse<BOEventWithFacilitiesResponse>> getEventWithFacilitiesForBO(
            @PathVariable Integer id) {

        BOEventWithFacilitiesResponse response = facilityApplicationService.getEventWithFacilitiesForBO(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event details retrieved successfully", response));
    }
}