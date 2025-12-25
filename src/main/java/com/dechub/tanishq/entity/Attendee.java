package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendees")
public class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    @Column(name = "`like`")
    private String like;
    private Boolean firstTimeAtTanishq;
    private LocalDateTime createdAt;
    private Boolean isUploadedFromExcel;
    private String rsoName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false, columnDefinition = "VARCHAR(255)")
    private Event event;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public Boolean getFirstTimeAtTanishq() {
        return firstTimeAtTanishq;
    }

    public void setFirstTimeAtTanishq(Boolean firstTimeAtTanishq) {
        this.firstTimeAtTanishq = firstTimeAtTanishq;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsUploadedFromExcel() {
        return isUploadedFromExcel;
    }

    public void setIsUploadedFromExcel(Boolean isUploadedFromExcel) {
        this.isUploadedFromExcel = isUploadedFromExcel;
    }

    public String getRsoName() {
        return rsoName;
    }

    public void setRsoName(String rsoName) {
        this.rsoName = rsoName;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
