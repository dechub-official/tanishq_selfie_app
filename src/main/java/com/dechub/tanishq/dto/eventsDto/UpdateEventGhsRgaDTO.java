package com.dechub.tanishq.dto.eventsDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for updating event GHS/RGA information
 */
public class UpdateEventGhsRgaDTO {

    @NotBlank(message = "Event code is required")
    @Size(max = 50, message = "Event code must not exceed 50 characters")
    private String eventCode;

    @NotNull(message = "GHS/RGA count is required")
    @Min(value = 0, message = "GHS/RGA count cannot be negative")
    private Integer ghsRga;

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public Integer getGhsRga() {
        return ghsRga;
    }

    public void setGhsRga(Integer ghsRga) {
        this.ghsRga = ghsRga;
    }
}

