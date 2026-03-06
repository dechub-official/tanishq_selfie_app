package com.dechub.tanishq.dto.eventsDto;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
/**
 * DTO for updating event advance information
 */
public class UpdateEventAdvanceDTO {
    @NotBlank(message = "Event code is required")
    @Size(max = 50, message = "Event code must not exceed 50 characters")
    private String eventCode;
    @NotNull(message = "Advance amount is required")
    @Min(value = 0, message = "Advance amount cannot be negative")
    private Integer advance;

    public String getEventCode() {

        return eventCode;
    }
    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }
    public Integer getAdvance() {

        return advance;
    }
    public void setAdvance(Integer advance) {

        this.advance = advance;
    }
}