package com.dechub.tanishq.dto.eventsDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for updating event GMB information
 */
public class UpdateEventGmbDTO {

    @NotBlank(message = "Event code is required")
    @Size(max = 50, message = "Event code must not exceed 50 characters")
    private String eventCode;

    @NotNull(message = "GMB count is required")
    @Min(value = 0, message = "GMB count cannot be negative")
    private Integer gmb;

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public Integer getGmb() {
        return gmb;
    }

    public void setGmb(Integer gmb) {
        this.gmb = gmb;
    }
}

