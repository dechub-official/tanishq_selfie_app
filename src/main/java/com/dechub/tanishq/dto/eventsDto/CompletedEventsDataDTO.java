package com.dechub.tanishq.dto.eventsDto;

public class CompletedEventsDataDTO {
    private String createdAt;
    private String EventType;
    private String Invitees;
    private String Attendees;

    private String Id;

    public CompletedEventsDataDTO(String createdAt, String eventType, String invitees, String attendees, String id) {
        this.createdAt = createdAt;
        EventType = eventType;
        Invitees = invitees;
        Attendees = attendees;
        Id = id;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }



    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getEventType() {
        return EventType;
    }

    public void setEventType(String eventType) {
        EventType = eventType;
    }

    public String getInvitees() {
        return Invitees;
    }

    public void setInvitees(String invitees) {
        Invitees = invitees;
    }

    public String getAttendees() {
        return Attendees;
    }

    public void setAttendees(String attendees) {
        Attendees = attendees;
    }
}
