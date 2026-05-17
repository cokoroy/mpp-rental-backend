package com.mpp.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body when MPP creates a new announcement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    /**
     * Target audience: ALL, STUDENT, NON_STUDENT
     * Defaults to ALL if not specified
     */
    private String targetAudience = "ALL";

    /**
     * Optional: link to a specific event
     */
    private Long referenceId;
}