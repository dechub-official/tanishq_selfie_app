package com.dechub.tanishq.dto.eventsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventsDetailDTO {
    private String storeCode;
    private String region;
    private String id;
    private String eventType;
    private String eventSubType;
    private String eventName;
    private String rso;
    private String startDate;
    private String startTime;
    private String description;
    private String image;
    private int invitees;
    private int attendees;
    private LocalDateTime createdAt;
    private String completedEventsDriveLink;
    private String community;
    private String location;
    private boolean isAttendeesUploaded;
    private String name;
    private String contact;
    private Integer sale;
    private Integer advance;
    private Integer ghsOrRga;
    private Integer gmb;
    private MultipartFile file;

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

    private boolean isSingleCustomer =true;



    public boolean getSingleCustomer() {
        return isSingleCustomer;
    }

    public void setSingleCustomer(boolean singleCustomer) {
        isSingleCustomer = singleCustomer;
    }


    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventSubType() {
        return eventSubType;
    }

    public void setEventSubType(String eventSubType) {
        this.eventSubType = eventSubType;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getRso() {
        return rso;
    }

    public void setRso(String rso) {
        this.rso = rso;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getInvitees() {
        return invitees;
    }

    public void setInvitees(int invitees) {
        this.invitees = invitees;
    }

    public int getAttendees() {
        return attendees;
    }

    public void setAttendees(int attendees) {
        this.attendees = attendees;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCompletedEventsDriveLink() {
        return completedEventsDriveLink;
    }

    public void setCompletedEventsDriveLink(String completedEventsDriveLink) {
        this.completedEventsDriveLink = completedEventsDriveLink;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
