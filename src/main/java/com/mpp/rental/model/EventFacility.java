package com.mpp.rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "EVENT_FACILITY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventFacilityId")
    private Integer eventFacilityId;

    @NotNull(message = "Event is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", nullable = false)
    private Event event;

    @NotNull(message = "Facility is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facilityId", nullable = false)
    private Facility facility;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(name = "quantityFacilityAvailable", nullable = false)
    private Integer quantityFacilityAvailable;

    @NotNull(message = "Student price is required")
    @DecimalMin(value = "0.00", message = "Student price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Student price must have at most 2 decimal places")
    @Column(name = "facilityStudentPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal facilityStudentPrice;

    @NotNull(message = "Non-student price is required")
    @DecimalMin(value = "0.00", message = "Non-student price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Non-student price must have at most 2 decimal places")
    @Column(name = "facilityNonStudentPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal facilityNonStudentPrice;

    @NotNull(message = "Max per business is required")
    @Min(value = 1, message = "Max per business must be at least 1")
    @Column(name = "maxPerBusiness", nullable = false)
    private Integer maxPerBusiness;
}
