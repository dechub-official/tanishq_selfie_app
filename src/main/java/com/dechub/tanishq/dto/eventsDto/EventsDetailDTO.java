package com.dechub.tanishq.dto.eventsDto;

import com.dechub.tanishq.validation.ValidPhone;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventsDetailDTO {

    @NotBlank(message = "Store code is required")
    @Size(max = 50, message = "Store code must not exceed 50 characters")
    private String storeCode;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    @Size(max = 50, message = "ID must not exceed 50 characters")
    private String id;

    @NotBlank(message = "Event type is required")
    @Size(max = 100, message = "Event type must not exceed 100 characters")
    private String eventType;

    @Size(max = 100, message = "Event sub-type must not exceed 100 characters")
    private String eventSubType;

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 200, message = "Event name must be between 3 and 200 characters")
    private String eventName;

    @Size(max = 100, message = "RSO must not exceed 100 characters")
    private String rso;

    @NotBlank(message = "Start date is required")
    @Size(max = 20, message = "Start date must not exceed 20 characters")
    private String startDate;

    // Start time is optional - default value "00:00" will be used if not provided
    @Size(max = 20, message = "Start time must not exceed 20 characters")
    private String startTime;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Image path must not exceed 500 characters")
    private String image;

    @Min(value = 0, message = "Invitees count cannot be negative")
    @Max(value = 10000, message = "Invitees count cannot exceed 10000")
    private int invitees;

    @Min(value = 0, message = "Attendees count cannot be negative")
    @Max(value = 10000, message = "Attendees count cannot exceed 10000")
    private int attendees;

    private LocalDateTime createdAt;

    @Size(max = 500, message = "Drive link must not exceed 500 characters")
    private String completedEventsDriveLink;

    @Size(max = 100, message = "Community must not exceed 100 characters")
    private String community;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    private boolean isAttendeesUploaded;

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]*$", message = "Name can only contain letters, spaces, and basic punctuation")
    private String name;

    @ValidPhone
    private String contact;

    @Min(value = 0, message = "Sale amount cannot be negative")
    @Max(value = 100000000, message = "Sale amount is too large")
    private Integer sale;

    @Min(value = 0, message = "Advance amount cannot be negative")
    @Max(value = 100000000, message = "Advance amount is too large")
    private Integer advance;

    @Min(value = 0, message = "GHS/RGA count cannot be negative")
    @Max(value = 10000, message = "GHS/RGA count cannot exceed 10000")
    private Integer ghsOrRga;

    @Min(value = 0, message = "GMB count cannot be negative")
    @Max(value = 10000, message = "GMB count cannot exceed 10000")
    private Integer gmb;

    private MultipartFile file;
    private boolean diamondAwareness;
    private boolean ghsFlag;


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

    public boolean isDiamondAwareness() {
        return diamondAwareness;
    }
    public void setDiamondAwareness(boolean diamondAwareness) {
        this.diamondAwareness = diamondAwareness;
    }

    public boolean isGhsFlag() {
        return ghsFlag;
    }
    public void setGhsFlag(boolean ghsFlag) {
        this.ghsFlag = ghsFlag;
    }
}
