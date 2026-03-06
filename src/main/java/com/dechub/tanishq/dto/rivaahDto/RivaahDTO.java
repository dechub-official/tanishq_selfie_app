package com.dechub.tanishq.dto.rivaahDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class RivaahDTO {

    @NotBlank(message = "Bride type is required")
    @Size(max = 50, message = "Bride type must not exceed 50 characters")
    private String bride;

    @NotBlank(message = "Event type is required")
    @Size(max = 100, message = "Event type must not exceed 100 characters")
    private String event;

    @Size(max = 50, message = "Clothing type must not exceed 50 characters")
    private String clothing_type;

    @Size(max = 20, message = "Cannot have more than 20 tags")
    private List<String> tags;

    public RivaahDTO(String bride, String event, String clothing_type, List<String> tags) {
        this.bride = bride;
        this.event = event;
        this.clothing_type = clothing_type;
        this.tags = tags;
    }

    public String getBride() {
        return bride;
    }

    public void setBride(String bride) {
        this.bride = bride;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getClothing_type() {
        return clothing_type;
    }

    public void setClothing_type(String clothing_type) {
        this.clothing_type = clothing_type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
