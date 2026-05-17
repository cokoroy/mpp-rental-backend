package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Integer configId;

    /** Group key — matches the constants below */
    @Column(name = "config_group", nullable = false, length = 50)
    private String configGroup;

    @Column(name = "config_value", nullable = false, length = 100)
    private String configValue;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Config group constants ────────────────────────────────
    public static final String GROUP_BUSINESS_CATEGORY = "BUSINESS_CATEGORY";
    public static final String GROUP_FACILITY_TYPE     = "FACILITY_TYPE";
    public static final String GROUP_FACILITY_SIZE     = "FACILITY_SIZE";
    public static final String GROUP_FACILITY_USAGE    = "FACILITY_USAGE";
    public static final String GROUP_EVENT_TYPE        = "EVENT_TYPE";
}