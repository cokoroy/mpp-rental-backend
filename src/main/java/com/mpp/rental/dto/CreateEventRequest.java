package com.mpp.rental.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 200, message = "Event name must be between 3 and 200 characters")
    private String eventName;

    @NotBlank(message = "Event venue is required")
    @Size(min = 3, max = 200, message = "Event venue must be between 3 and 200 characters")
    private String eventVenue;

    @NotNull(message = "Event start date is required")
    @FutureOrPresent(message = "Event start date must be today or in the future")
    private LocalDate eventStartDate;

    @NotNull(message = "Event end date is required")
    @FutureOrPresent(message = "Event end date must be today or in the future")
    private LocalDate eventEndDate;

    @NotNull(message = "Event start time is required")
    private LocalTime eventStartTime;

    @NotNull(message = "Event end time is required")
    private LocalTime eventEndTime;

    @NotBlank(message = "Event type is required")
    @Size(max = 100, message = "Event type cannot exceed 100 characters")
    private String eventType;

    // UPDATED: Removed minimum character requirement (was min = 10)
    @NotBlank(message = "Event description is required")
    @Size(max = 1000, message = "Event description cannot exceed 1000 characters")
    private String eventDesc;

    @NotEmpty(message = "At least one facility must be assigned")
    @Valid
    private List<AssignFacilityRequest> facilities;
}