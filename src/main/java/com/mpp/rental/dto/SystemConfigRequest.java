package com.mpp.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigRequest {

    @NotBlank(message = "Config group is required")
    private String configGroup;

    @NotBlank(message = "Value is required")
    @Size(max = 100, message = "Value must not exceed 100 characters")
    private String configValue;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    private Boolean isActive = true;
}