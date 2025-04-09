package com.dechub.tanishq.dto.rivaahDto;

import java.util.List;

public class RivaahDTO {
    private String bride;
    private String event;
    private String clothing_type;
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
