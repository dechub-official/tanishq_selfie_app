package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bride_details")
public class BrideDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brideName;
    private String brideEvent;
    private String email;
    private String phone;
    private LocalDate date; // or String
    private String brideType;
    private String zipCode;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrideName() {
        return brideName;
    }

    public void setBrideName(String brideName) {
        this.brideName = brideName;
    }

    public String getBrideEvent() {
        return brideEvent;
    }

    public void setBrideEvent(String brideEvent) {
        this.brideEvent = brideEvent;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getBrideType() {
        return brideType;
    }

    public void setBrideType(String brideType) {
        this.brideType = brideType;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
