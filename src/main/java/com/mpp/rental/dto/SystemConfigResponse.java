package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {
    private Integer       configId;
    private String        configGroup;
    private String        configValue;
    private Integer       displayOrder;
    private Boolean       isActive;
    private LocalDateTime createdAt;
}