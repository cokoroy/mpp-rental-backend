package com.mpp.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotBlank(message = "Ticket title is required")
    private String ticketTitle;

    @NotBlank(message = "Ticket description is required")
    private String ticketDescription;

    @NotNull(message = "Category is required")
    private String ticketCategory;

    @NotNull(message = "Priority is required")
    private String ticketPriority;
}