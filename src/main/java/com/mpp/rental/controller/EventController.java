package com.mpp.rental.controller;

import com.mpp.rental.dto.*;
import com.mpp.rental.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mpp/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    /**
     * Create new event with facility assignments
     * POST /api/mpp/events
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EventWithFacilitiesResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        try {
            EventWithFacilitiesResponse response = eventService.createEvent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Event created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get all events with optional search and filters
     * GET /api/mpp/events
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "all") String eventStatus) {
        
        EventSearchFilterRequest filter = new EventSearchFilterRequest(searchQuery, eventStatus);
        
        List<EventResponse> events = eventService.getAllEvents(filter);
        return ResponseEntity.ok(new ApiResponse<>(true, "Events retrieved successfully", events));
    }

    /**
     * Get event by ID
     * GET /api/mpp/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Integer id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event retrieved successfully", response));
    }

    /**
     * Get event with assigned facilities
     * GET /api/mpp/events/{id}/with-facilities
     */
    @GetMapping("/{id}/with-facilities")
    public ResponseEntity<ApiResponse<EventWithFacilitiesResponse>> getEventWithFacilities(
            @PathVariable Integer id) {
        EventWithFacilitiesResponse response = eventService.getEventWithFacilities(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event with facilities retrieved successfully", response));
    }

    /**
     * Get facilities assigned to an event
     * GET /api/mpp/events/{id}/facilities
     */
    @GetMapping("/{id}/facilities")
    public ResponseEntity<ApiResponse<List<EventFacilityResponse>>> getEventFacilities(
            @PathVariable Integer id) {
        List<EventFacilityResponse> facilities = eventService.getEventFacilities(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event facilities retrieved successfully", facilities));
    }

    /**
     * Update event
     * PUT /api/mpp/events/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventWithFacilitiesResponse>> updateEvent(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEventRequest request) {
        try {
            EventWithFacilitiesResponse response = eventService.updateEvent(id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Event updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Cancel event (soft delete)
     * DELETE /api/mpp/events/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelEvent(@PathVariable Integer id) {
        eventService.cancelEvent(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event cancelled successfully", null));
    }

    /**
     * Toggle event application status (OPEN <-> CLOSED)
     * PATCH /api/mpp/events/{id}/application-status
     */
    @PatchMapping("/{id}/application-status")
    public ResponseEntity<ApiResponse<EventResponse>> toggleApplicationStatus(@PathVariable Integer id) {
        EventResponse response = eventService.toggleApplicationStatus(id);
        String message = response.getEventApplicationStatus().equals("OPEN")
                ? "Applications opened for event"
                : "Applications closed for event";
        return ResponseEntity.ok(new ApiResponse<>(true, message, response));
    }
}
