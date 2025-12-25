package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @Column(length = 255, nullable = false)
    private String id; // event id like storeCode_uuid

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "region")
    private String region;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "event_sub_type")
    private String eventSubType;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "rso")
    private String rso;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "image")
    private String image;

    @Column(name = "invitees")
    private Integer invitees;

    @Column(name = "attendees")
    private Integer attendees;

    @Column(name = "completed_events_drive_link")
    private String completedEventsDriveLink;

    @Column(name = "community")
    private String community;

    @Column(name = "location")
    private String location;

    @Column(name = "attendees_uploaded")
    private Boolean attendeesUploaded;

    @Column(name = "sale")
    private Double sale;

    @Column(name = "advance")
    private Double advance;

    @Column(name = "ghs_or_rga")
    private Double ghsOrRga;

    @Column(name = "gmb")
    private Double gmb;

    @Column(name = "diamond_awareness")
    private Boolean diamondAwareness;

    @Column(name = "ghs_flag")
    private Boolean ghsFlag;

    @ManyToOne
    @JoinColumn(name = "store_code")
    private Store store;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Attendee> attendeesEntities;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Invitee> inviteesEntities;

    // getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getInvitees() {
        return invitees;
    }

    public void setInvitees(Integer invitees) {
        this.invitees = invitees;
    }

    public Integer getAttendees() {
        return attendees;
    }

    public void setAttendees(Integer attendees) {
        this.attendees = attendees;
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

    public Boolean getAttendeesUploaded() {
        return attendeesUploaded;
    }

    public void setAttendeesUploaded(Boolean attendeesUploaded) {
        this.attendeesUploaded = attendeesUploaded;
    }

    public Double getSale() {
        return sale;
    }

    public void setSale(Double sale) {
        this.sale = sale;
    }

    public Double getAdvance() {
        return advance;
    }

    public void setAdvance(Double advance) {
        this.advance = advance;
    }

    public Double getGhsOrRga() {
        return ghsOrRga;
    }

    public void setGhsOrRga(Double ghsOrRga) {
        this.ghsOrRga = ghsOrRga;
    }

    public Double getGmb() {
        return gmb;
    }

    public void setGmb(Double gmb) {
        this.gmb = gmb;
    }

    public Boolean getDiamondAwareness() {
        return diamondAwareness;
    }

    public void setDiamondAwareness(Boolean diamondAwareness) {
        this.diamondAwareness = diamondAwareness;
    }

    public Boolean getGhsFlag() {
        return ghsFlag;
    }

    public void setGhsFlag(Boolean ghsFlag) {
        this.ghsFlag = ghsFlag;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public List<Attendee> getAttendeesEntities() {
        return attendeesEntities;
    }

    public void setAttendeesEntities(List<Attendee> attendeesEntities) {
        this.attendeesEntities = attendeesEntities;
    }

    public List<Invitee> getInviteesEntities() {
        return inviteesEntities;
    }

    public void setInviteesEntities(List<Invitee> inviteesEntities) {
        this.inviteesEntities = inviteesEntities;
    }
}
