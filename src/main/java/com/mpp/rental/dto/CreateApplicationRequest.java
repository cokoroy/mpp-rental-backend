package com.mpp.rental.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "At least one facility must be selected")
    private List<FacilityApplicationItem> facilities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityApplicationItem {

        @NotNull(message = "Event facility ID is required")
        private Integer eventFacilityId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}