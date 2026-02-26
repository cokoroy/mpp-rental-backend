package com.mpp.rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "FACILITY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE facility SET deleted_at = NOW() WHERE facility_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facilityId")
    private Integer facilityId;

    @NotBlank(message = "Facility name is required")
    @Size(min = 3, max = 100, message = "Facility name must be between 3 and 100 characters")
    @Column(name = "facilityName", nullable = false, unique = true, length = 100)
    private String facilityName;

    @NotBlank(message = "Facility size is required")
    @Size(min = 2, max = 50, message = "Facility size must be between 2 and 50 characters")
    @Column(name = "facilitySize", nullable = false, length = 50)
    private String facilitySize;

    @NotBlank(message = "Facility type is required")
    @Size(min = 2, max = 50, message = "Facility type must be between 2 and 50 characters")
    @Column(name = "facilityType", nullable = false, length = 50)
    private String facilityType;

    @NotBlank(message = "Facility description is required")
    @Size(min = 1, max = 500, message = "Facility description must be between 10 and 500 characters")
    @Column(name = "facilityDesc", nullable = false, length = 500)
    private String facilityDesc;

    @NotBlank(message = "Usage information is required")
    @Size(min = 5, max = 500, message = "Usage information must be between 5 and 500 characters")
    @Column(name = "facility_usage ", nullable = false, length = 500)
    private String facility_usage ;

    @Size(max = 500, message = "Remark must not exceed 500 characters")
    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "facilityImage", length = 255)
    private String facilityImage;

    @NotNull(message = "Student price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Student price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Student price must have at most 2 decimal places")
    @Column(name = "facilityBaseStudentPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal facilityBaseStudentPrice;

    @NotNull(message = "Non-student price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Student price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Non-student price must have at most 2 decimal places")
    @Column(name = "facilityBaseNonstudentPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal facilityBaseNonstudentPrice;

    @NotBlank(message = "Facility status is required")
    @Pattern(regexp = "active|inactive", message = "Status must be either 'active' or 'inactive'")
    @Column(name = "facilityStatus", nullable = false, length = 20)
    private String facilityStatus;

    @CreationTimestamp
    @Column(name = "facilityCreateAt", nullable = false, updatable = false)
    private LocalDateTime facilityCreateAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void createFacility() {
        if (this.facilityStatus == null) {
            this.facilityStatus = "active";
        }
    }
}