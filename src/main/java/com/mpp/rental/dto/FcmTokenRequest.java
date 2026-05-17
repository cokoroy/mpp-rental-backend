package com.mpp.rental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body when frontend registers a new FCM token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    private String deviceInfo; // optional e.g. "Chrome on Windows"
}