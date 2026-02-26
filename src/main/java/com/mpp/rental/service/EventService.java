package com.mpp.rental.service;


import com.mpp.rental.dto.*;
import com.mpp.rental.exception.BadRequestException;
import com.mpp.rental.exception.DuplicateEventException;
import com.mpp.rental.exception.EventFacilityException;
import com.mpp.rental.exception.EventNotFoundException;
import com.mpp.rental.model.Event;
import com.mpp.rental.model.EventFacility;
import com.mpp.rental.model.Facility;
import com.mpp.rental.repository.EventFacilityRepository;
import com.mpp.rental.repository.EventRepository;
import com.mpp.rental.repository.FacilityRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventFacilityRepository eventFacilityRepository;
    private final FacilityRepository facilityRepository;

    /**
     * Create new event with facility assignments
     */
    @Transactional
    public EventWithFacilitiesResponse createEvent(CreateEventRequest request) {
        // Validate event dates
        validateEventDates(request.getEventStartDate(), request.getEventEndDate(), 
                          request.getEventStartTime(), request.getEventEndTime());

        // Check for duplicate event name
        if (eventRepository.existsByEventNameIgnoreCaseAndDeletedAtIsNull(request.getEventName())) {
            throw new DuplicateEventException(request.getEventName());
        }

        // Validate that at least one facility is assigned
        if (request.getFacilities() == null || request.getFacilities().isEmpty()) {
            throw new BadRequestException("At least one facility must be assigned to the event");
        }

        // Create event entity
        Event event = new Event();
        event.setEventName(request.getEventName());
        event.setEventVenue(request.getEventVenue());
        event.setEventStartDate(request.getEventStartDate());
        event.setEventEndDate(request.getEventEndDate());
        event.setEventStartTime(request.getEventStartTime());
        event.setEventEndTime(request.getEventEndTime());
        event.setEventType(request.getEventType());
        event.setEventDesc(request.getEventDesc());
        event.setEventApplicationStatus("OPEN");
        
        // Determine initial status based on start date
        event.updateStatusBasedOnDates();

        // Save event
        Event savedEvent = eventRepository.save(event);

        // Assign facilities
        List<EventFacility> assignedFacilities = assignFacilitiesToEvent(savedEvent, request.getFacilities());

        // Build response
        return buildEventWithFacilitiesResponse(savedEvent, assignedFacilities);
    }

    /**
     * Get all events with search and filters
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents(EventSearchFilterRequest filter) {
        Specification<Event> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude deleted events (always apply)
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            // Apply search query (search by event name)
            if (filter.getSearchQuery() != null && !filter.getSearchQuery().trim().isEmpty()) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("eventName")),
                        "%" + filter.getSearchQuery().toLowerCase() + "%"
                    )
                );
            }

            // Apply status filter
            if (filter.getEventStatus() != null && !filter.getEventStatus().equals("all")) {
                predicates.add(criteriaBuilder.equal(root.get("eventStatus"), filter.getEventStatus()));
            }

            // Hide completed and cancelled events older than 1 month
            LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
            Predicate notOldCompleted = criteriaBuilder.or(
                criteriaBuilder.notEqual(root.get("eventStatus"), "completed"),
                criteriaBuilder.greaterThanOrEqualTo(root.get("eventEndDate"), oneMonthAgo)
            );
            Predicate notOldCancelled = criteriaBuilder.or(
                criteriaBuilder.notEqual(root.get("eventStatus"), "cancelled"),
                criteriaBuilder.greaterThanOrEqualTo(root.get("eventEndDate"), oneMonthAgo)
            );
            predicates.add(criteriaBuilder.and(notOldCompleted, notOldCancelled));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Execute query with sorting (newest first)
        Sort sort = Sort.by(Sort.Direction.DESC, "eventCreateAt");
        List<Event> events = eventRepository.findAll(spec, sort);

        // Update statuses based on current date
        events.forEach(Event::updateStatusBasedOnDates);
        eventRepository.saveAll(events);

        return events.stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get event by ID
     */
    @Transactional(readOnly = true)
    public EventResponse getEventById(Integer eventId) {
        Event event = eventRepository.findByEventIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        
        // Update status based on current date
        event.updateStatusBasedOnDates();
        eventRepository.save(event);

        return mapToEventResponse(event);
    }

    /**
     * Get event with assigned facilities
     */
    @Transactional(readOnly = true)
    public EventWithFacilitiesResponse getEventWithFacilities(Integer eventId) {
        Event event = eventRepository.findByEventIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        
        // Update status based on current date
        event.updateStatusBasedOnDates();
        eventRepository.save(event);

        List<EventFacility> facilities = eventFacilityRepository.findByEventIdWithFacility(eventId);

        return buildEventWithFacilitiesResponse(event, facilities);
    }

    /**
     * Update event
     */
    @Transactional
    public EventWithFacilitiesResponse updateEvent(Integer eventId, UpdateEventRequest request) {
        // Find existing event
        Event event = eventRepository.findByEventIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Check if event can be edited
        if (event.getEventStatus().equals("completed")) {
            throw new BadRequestException("Cannot edit completed events");
        }
        if (event.getEventStatus().equals("cancelled")) {
            throw new BadRequestException("Cannot edit cancelled events");
        }

        // Check for duplicate name (excluding current event)
        if (eventRepository.existsByEventNameAndNotId(request.getEventName(), eventId)) {
            throw new DuplicateEventException(request.getEventName());
        }

        // Update basic fields
        event.setEventName(request.getEventName());
        event.setEventVenue(request.getEventVenue());
        event.setEventStartTime(request.getEventStartTime());
        event.setEventEndTime(request.getEventEndTime());
        event.setEventType(request.getEventType());
        event.setEventDesc(request.getEventDesc());

        // Update dates only if event is still upcoming
        if (event.getEventStatus().equals("upcoming")) {
            if (request.getEventStartDate() == null || request.getEventEndDate() == null) {
                throw new BadRequestException("Start date and end date are required for upcoming events");
            }
            validateEventDates(request.getEventStartDate(), request.getEventEndDate(),
                             request.getEventStartTime(), request.getEventEndTime());
            event.setEventStartDate(request.getEventStartDate());
            event.setEventEndDate(request.getEventEndDate());
        } else if (event.getEventStatus().equals("active")) {
            // For active events, dates cannot be changed
            // Validate that times are still logical
            validateEventTimes(request.getEventStartTime(), request.getEventEndTime());
        }

        // Update status based on dates
        event.updateStatusBasedOnDates();

        // Save event
        Event updatedEvent = eventRepository.save(event);

        // Update facility assignments
        updateEventFacilities(eventId, request.getFacilities());

        // Get updated facilities
        List<EventFacility> facilities = eventFacilityRepository.findByEventIdWithFacility(eventId);

        return buildEventWithFacilitiesResponse(updatedEvent, facilities);
    }

    /**
     */



    /**
     * Cancel event (soft delete)
     */
    @Transactional
    public void cancelEvent(Integer eventId) {
        Event event = eventRepository.findByEventIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Only upcoming events can be cancelled
        if (event.getEventStatus().equals("active")) {
            throw new BadRequestException("Cannot cancel active events");
        }
        if (event.getEventStatus().equals("completed")) {
            throw new BadRequestException("Cannot cancel completed events");
        }
        if (event.getEventStatus().equals("cancelled")) {
            throw new BadRequestException("Event is already cancelled");
        }

        // Set status to cancelled instead of deleting
        event.setEventStatus("cancelled");
        event.setDeletedAt(LocalDateTime.now());
        eventRepository.save(event);
    }

    /**
     * Toggle event application status (OPEN <-> CLOSED)
     */
    @Transactional
    public EventResponse toggleApplicationStatus(Integer eventId) {
        Event event = eventRepository.findByEventIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Can only toggle for upcoming and active events
        if (!event.getEventStatus().equals("upcoming") && !event.getEventStatus().equals("active")) {
            throw new BadRequestException("Can only toggle application status for upcoming or active events");
        }

        // Toggle status
        String newStatus = event.getEventApplicationStatus().equals("OPEN") ? "CLOSED" : "OPEN";
        event.setEventApplicationStatus(newStatus);

        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponse(updatedEvent);
    }

    /**
     * Get facilities assigned to an event
     */
    @Transactional(readOnly = true)
    public List<EventFacilityResponse> getEventFacilities(Integer eventId) {
        // Verify event exists
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }

        List<EventFacility> facilities = eventFacilityRepository.findByEventIdWithFacility(eventId);

        return facilities.stream()
                .map(this::mapToEventFacilityResponse)
                .collect(Collectors.toList());
    }

    /**
     * Assign facilities to an event (helper method)
     */
    private List<EventFacility> assignFacilitiesToEvent(Event event, List<AssignFacilityRequest> facilityRequests) {
        List<EventFacility> assignedFacilities = new ArrayList<>();

        for (AssignFacilityRequest facilityRequest : facilityRequests) {
            // Find facility
            Facility facility = facilityRepository.findByFacilityIdAndDeletedAtIsNull(facilityRequest.getFacilityId())
                    .orElseThrow(() -> new BadRequestException("Facility not found with ID: " + facilityRequest.getFacilityId()));

            // Check if facility is active
            if (!facility.getFacilityStatus().equals("active")) {
                throw new BadRequestException("Cannot assign inactive facility: " + facility.getFacilityName());
            }

            // Create EventFacility
            EventFacility eventFacility = new EventFacility();
            eventFacility.setEvent(event);
            eventFacility.setFacility(facility);
            eventFacility.setQuantityFacilityAvailable(facilityRequest.getQuantity());
            eventFacility.setMaxPerBusiness(facilityRequest.getMaxPerBusiness());
            eventFacility.setFacilityStudentPrice(facilityRequest.getStudentPrice());
            eventFacility.setFacilityNonStudentPrice(facilityRequest.getNonStudentPrice());

            assignedFacilities.add(eventFacilityRepository.save(eventFacility));
        }

        return assignedFacilities;
    }

    /**
     * Update event facilities (for edit functionality)
     */
    private void updateEventFacilities(Integer eventId, List<AssignFacilityRequest> facilityRequests) {
        // Validate that at least one facility is assigned
        if (facilityRequests == null || facilityRequests.isEmpty()) {
            throw new BadRequestException("At least one facility must be assigned to the event");
        }

        // Get existing facility assignments
        List<EventFacility> existingFacilities = eventFacilityRepository.findByEventIdWithFacility(eventId);

        // Process each facility request
        for (AssignFacilityRequest request : facilityRequests) {
            if (request.getEventFacilityId() != null) {
                // Update existing facility
                EventFacility existing = existingFacilities.stream()
                        .filter(ef -> ef.getEventFacilityId().equals(request.getEventFacilityId()))
                        .findFirst()
                        .orElseThrow(() -> new EventFacilityException("Event facility not found: " + request.getEventFacilityId()));

                existing.setQuantityFacilityAvailable(request.getQuantity());
                existing.setMaxPerBusiness(request.getMaxPerBusiness());
                existing.setFacilityStudentPrice(request.getStudentPrice());
                existing.setFacilityNonStudentPrice(request.getNonStudentPrice());
                eventFacilityRepository.save(existing);
            } else {
                // Add new facility
                Facility facility = facilityRepository.findByFacilityIdAndDeletedAtIsNull(request.getFacilityId())
                        .orElseThrow(() -> new BadRequestException("Facility not found with ID: " + request.getFacilityId()));

                if (!facility.getFacilityStatus().equals("active")) {
                    throw new BadRequestException("Cannot assign inactive facility: " + facility.getFacilityName());
                }

                Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new EventNotFoundException(eventId));

                EventFacility newEventFacility = new EventFacility();
                newEventFacility.setEvent(event);
                newEventFacility.setFacility(facility);
                newEventFacility.setQuantityFacilityAvailable(request.getQuantity());
                newEventFacility.setMaxPerBusiness(request.getMaxPerBusiness());
                newEventFacility.setFacilityStudentPrice(request.getStudentPrice());
                newEventFacility.setFacilityNonStudentPrice(request.getNonStudentPrice());
                eventFacilityRepository.save(newEventFacility);
            }
        }

        // Remove facilities not in the request
        List<Integer> requestedFacilityIds = facilityRequests.stream()
                .filter(r -> r.getEventFacilityId() != null)
                .map(AssignFacilityRequest::getEventFacilityId)
                .collect(Collectors.toList());

        for (EventFacility existing : existingFacilities) {
            if (!requestedFacilityIds.contains(existing.getEventFacilityId())) {
                // Check if facility has applications
                if (eventFacilityRepository.hasApplications(existing.getEventFacilityId())) {
                    throw new EventFacilityException(
                        "Cannot remove facility '" + existing.getFacility().getFacilityName() + 
                        "' because it has existing applications"
                    );
                }
                eventFacilityRepository.delete(existing);
            }
        }
    }

    /**
     * Validate event dates and times
     */
    private void validateEventDates(LocalDate startDate, LocalDate endDate, 
                                    LocalTime startTime, LocalTime endTime) {
        LocalDate today = LocalDate.now();

        // Check if dates are in the past
        if (startDate.isBefore(today)) {
            throw new BadRequestException("Event start date cannot be in the past");
        }

        // Check if end date is before start date
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("Event end date must be after start date");
        }

        // If same day event, validate times
        if (startDate.equals(endDate)) {
            validateEventTimes(startTime, endTime);
        }
    }

    /**
     * Validate event times
     */
    private void validateEventTimes(LocalTime startTime, LocalTime endTime) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new BadRequestException("Event end time must be after start time");
        }
    }

    /**
     * Scheduled task to auto-update event statuses
     * Runs every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoUpdateEventStatuses() {
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findEventsNeedingStatusUpdate(today);

        for (Event event : events) {
            event.updateStatusBasedOnDates();
        }

        eventRepository.saveAll(events);
    }

    /**
     * Map Event to EventResponse
     */
    private EventResponse mapToEventResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventVenue(event.getEventVenue());
        response.setEventStartDate(event.getEventStartDate());
        response.setEventEndDate(event.getEventEndDate());
        response.setEventStartTime(event.getEventStartTime());
        response.setEventEndTime(event.getEventEndTime());
        response.setEventType(event.getEventType());
        response.setEventDesc(event.getEventDesc());
        response.setEventApplicationStatus(event.getEventApplicationStatus());
        response.setEventStatus(event.getEventStatus());
        response.setEventCreateAt(event.getEventCreateAt());
        return response;
    }

    /**
     * Map EventFacility to EventFacilityResponse
     */
    private EventFacilityResponse mapToEventFacilityResponse(EventFacility eventFacility) {
        Facility facility = eventFacility.getFacility();
        
        EventFacilityResponse response = new EventFacilityResponse();
        response.setEventFacilityId(eventFacility.getEventFacilityId());
        response.setEventId(eventFacility.getEvent().getEventId());
        response.setFacilityId(facility.getFacilityId());
        response.setFacilityName(facility.getFacilityName());
        response.setFacilitySize(facility.getFacilitySize());
        response.setFacilityType(facility.getFacilityType());
        response.setFacilityDesc(facility.getFacilityDesc());
        response.setFacilityUsage(facility.getFacility_usage());
        response.setQuantityFacilityAvailable(eventFacility.getQuantityFacilityAvailable());
        response.setFacilityStudentPrice(eventFacility.getFacilityStudentPrice());
        response.setFacilityNonStudentPrice(eventFacility.getFacilityNonStudentPrice());
        response.setMaxPerBusiness(eventFacility.getMaxPerBusiness());
        return response;
    }

    /**
     * Build EventWithFacilitiesResponse
     */
    private EventWithFacilitiesResponse buildEventWithFacilitiesResponse(Event event, List<EventFacility> facilities) {
        EventWithFacilitiesResponse response = new EventWithFacilitiesResponse();
        response.setEventId(event.getEventId());
        response.setEventName(event.getEventName());
        response.setEventVenue(event.getEventVenue());
        response.setEventStartDate(event.getEventStartDate());
        response.setEventEndDate(event.getEventEndDate());
        response.setEventStartTime(event.getEventStartTime());
        response.setEventEndTime(event.getEventEndTime());
        response.setEventType(event.getEventType());
        response.setEventDesc(event.getEventDesc());
        response.setEventApplicationStatus(event.getEventApplicationStatus());
        response.setEventStatus(event.getEventStatus());
        response.setEventCreateAt(event.getEventCreateAt());
        
        List<EventFacilityResponse> facilityResponses = facilities.stream()
                .map(this::mapToEventFacilityResponse)
                .collect(Collectors.toList());
        response.setFacilities(facilityResponses);
        
        return response;
    }
}
