package com.mpp.rental.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MPPApplicationActionRequest {

    // For single reject — optional reason
    private String rejectionReason;

    // For bulk actions
    private List<Integer> applicationIds;
}