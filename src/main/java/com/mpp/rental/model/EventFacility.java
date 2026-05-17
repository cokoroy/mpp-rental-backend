package com.mpp.rental.model;

import jakarta.persistence.*;
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
    @Column(name = "event_facility_id")
    private Integer eventFacilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    // Allocation mode flag
    @Column(name = "is_allocated_by_category", nullable = false)
    private Boolean isAllocatedByCategory = false;

    // CURRENT quantities (decrease as applications are approved)
    @Column(name = "quantity_student_available", nullable = false)
    private Integer quantityStudentAvailable = 0;

    @Column(name = "quantity_non_student_available", nullable = false)
    private Integer quantityNonStudentAvailable = 0;

    @Column(name = "quantity_facility_available", nullable = false)
    private Integer quantityFacilityAvailable;

    // NEW: ORIGINAL quantities (set when event facility is created, never changes)
    @Column(name = "original_quantity_student")
    private Integer originalQuantityStudent;

    @Column(name = "original_quantity_non_student")
    private Integer originalQuantityNonStudent;

    @Column(name = "original_quantity_total")
    private Integer originalQuantityTotal;

    @Column(name = "facility_student_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal facilityStudentPrice;

    @Column(name = "facility_non_student_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal facilityNonStudentPrice;

    @Column(name = "max_per_business", nullable = false)
    private Integer maxPerBusiness;

    /**
     * Auto-calculate total quantity based on allocation mode
     * AND set original quantities when first created
     */
    @PrePersist
    protected void onCreate() {
        calculateTotalQuantity();

        // Set original quantities (only on first save)
        if (this.originalQuantityStudent == null) {
            this.originalQuantityStudent = this.quantityStudentAvailable;
        }
        if (this.originalQuantityNonStudent == null) {
            this.originalQuantityNonStudent = this.quantityNonStudentAvailable;
        }
        if (this.originalQuantityTotal == null) {
            this.originalQuantityTotal = this.quantityFacilityAvailable;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotalQuantity();
        // Don't update original quantities - they should never change after creation
    }

    private void calculateTotalQuantity() {
        if (this.isAllocatedByCategory != null && this.isAllocatedByCategory) {
            // Mode 1: Allocated by category - sum of student + non-student
            if (this.quantityStudentAvailable != null && this.quantityNonStudentAvailable != null) {
                this.quantityFacilityAvailable = this.quantityStudentAvailable + this.quantityNonStudentAvailable;
            }
        }
        // Mode 2: Open to all - quantityFacilityAvailable is used directly (no calculation needed)
    }
}