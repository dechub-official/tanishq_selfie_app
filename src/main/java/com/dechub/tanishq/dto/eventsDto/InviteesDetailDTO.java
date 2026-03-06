package com.dechub.tanishq.dto.eventsDto;

import com.dechub.tanishq.validation.ValidPhone;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class InviteesDetailDTO {

    @NotBlank(message = "Event ID is required")
    @Size(max = 50, message = "Event ID must not exceed 50 characters")
    private String eventId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Name can only contain letters, spaces, and basic punctuation")
    private String name;

    @NotBlank(message = "Contact number is required")
    @ValidPhone
    private String contact;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
