package com.dechub.tanishq.dto.eventsDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for updating event sale information
 */
public class UpdateEventSaleDTO {

    @NotBlank(message = "Event code is required")
    @Size(max = 50, message = "Event code must not exceed 50 characters")
    private String eventCode;

    @NotNull(message = "Sale amount is required")
    @Min(value = 0, message = "Sale amount cannot be negative")
    private Integer sale;

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public Integer getSale() {
        return sale;
    }

    public void setSale(Integer sale) {
        this.sale = sale;
    }
}

