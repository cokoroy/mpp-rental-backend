package com.mpp.rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "EVENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE event SET deleted_at = NOW() WHERE event_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId")
    private Integer eventId;

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 200, message = "Event name must be between 3 and 200 characters")
    @Column(name = "eventName", nullable = false, length = 200)
    private String eventName;

    @NotBlank(message = "Event venue is required")
    @Size(min = 3, max = 200, message = "Event venue must be between 3 and 200 characters")
    @Column(name = "eventVenue", nullable = false, length = 200)
    private String eventVenue;

    @NotNull(message = "Event start date is required")
    @Column(name = "eventStartDate", nullable = false)
    private LocalDate eventStartDate;

    @NotNull(message = "Event end date is required")
    @Column(name = "eventEndDate", nullable = false)
    private LocalDate eventEndDate;

    @NotNull(message = "Event start time is required")
    @Column(name = "eventStartTime", nullable = false)
    private LocalTime eventStartTime;

    @NotNull(message = "Event end time is required")
    @Column(name = "eventEndTime", nullable = false)
    private LocalTime eventEndTime;

    @NotBlank(message = "Event type is required")
    @Size(max = 100, message = "Event type must not exceed 100 characters")
    @Column(name = "eventType", nullable = false, length = 100)
    private String eventType;

    @NotBlank(message = "Event description is required")
    @Size(min = 10, max = 1000, message = "Event description must be between 10 and 1000 characters")
    @Column(name = "eventDesc", nullable = false, length = 1000)
    private String eventDesc;

    @NotBlank(message = "Event application status is required")
    @Pattern(regexp = "OPEN|CLOSED", message = "Application status must be either 'OPEN' or 'CLOSED'")
    @Column(name = "eventApplicationStatus", nullable = false, length = 20)
    private String eventApplicationStatus;

    @NotBlank(message = "Event status is required")
    @Pattern(regexp = "upcoming|active|completed|cancelled", message = "Status must be 'upcoming', 'active', 'completed', or 'cancelled'")
    @Column(name = "eventStatus", nullable = false, length = 20)
    private String eventStatus;

    @CreationTimestamp
    @Column(name = "eventCreateAt", nullable = false, updatable = false)
    private LocalDateTime eventCreateAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void createEvent() {
        // Set defaults FIRST
        if (this.eventApplicationStatus == null) {
            this.eventApplicationStatus = "OPEN";
        }
        if (this.eventStatus == null) {
            this.eventStatus = "upcoming";
        }

        // THEN update status based on dates
        updateStatusBasedOnDates();
    }

    /**
     * Update event status based on current date
     */
    public void updateStatusBasedOnDates() {
        // Check if status is null (safety check)
        if (this.eventStatus == null) {
            this.eventStatus = "upcoming";
        }

        // Don't change status if already cancelled
        if ("cancelled".equals(this.eventStatus)) {
            return;
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(this.eventStartDate)) {
            this.eventStatus = "upcoming";
        } else if (today.isAfter(this.eventEndDate)) {
            this.eventStatus = "completed";
        } else {
            this.eventStatus = "active";
        }
    }
}