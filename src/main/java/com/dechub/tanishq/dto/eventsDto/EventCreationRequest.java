package com.dechub.tanishq.dto.eventsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

/**
 * DTO for JSON-based event creation from frontend
 * This accepts the JSON payload sent by the frontend
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventCreationRequest {

    @NotBlank(message = "Store code is required")
    @Size(max = 50, message = "Store code must not exceed 50 characters")
    private String storeCode;

    @NotBlank(message = "Event type is required")
    @Size(max = 100, message = "Event type must not exceed 100 characters")
    private String eventType;

    @Size(max = 100, message = "Event sub-type must not exceed 100 characters")
    private String eventSubType;

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 200, message = "Event name must be between 3 and 200 characters")
    private String eventName;

    @Size(max = 100, message = "RSO must not exceed 100 characters")
    private String RSO; // Note: Frontend sends "RSO" with capitals

    @NotBlank(message = "Date is required")
    private String date; // Frontend sends "date" not "startDate"

    private String time; // Optional - can be empty string

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 100, message = "Community must not exceed 100 characters")
    private String Community; // Note: Frontend sends "Community" with capital C

    @Size(max = 500, message = "Image path must not exceed 500 characters")
    private String image;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String singleInvite; // "true" or "false" as string

    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    @Size(max = 20, message = "Customer contact must not exceed 20 characters")
    private String customerContact;

    private String customInvite; // "true" or "false" as string
    private String diamondAwareness; // "true" or "false" as string
    private String ghsFlag; // "true" or "false" as string

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    private Integer sale;
    private Integer advance;
    private Integer ghsOrRga;
    private Integer gmb;

    private String createdAt; // Frontend sends this, but we'll override on server

    // Helper methods to convert string booleans to actual booleans
    public boolean isSingleInvite() {
        return "true".equalsIgnoreCase(singleInvite);
    }

    public boolean isDiamondAwareness() {
        return "true".equalsIgnoreCase(diamondAwareness);
    }

    public boolean isGhsFlag() {
        return "true".equalsIgnoreCase(ghsFlag);
    }
}

