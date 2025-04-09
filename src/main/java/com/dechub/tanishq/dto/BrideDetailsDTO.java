package com.dechub.tanishq.dto;

public class BrideDetailsDTO {

    private String brideType;
    private String brideEvent;
    private String brideName;
    private String phone;
    private String date;
    private String email;

    private String zipCode;

    // Getters and setters

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

    public String getBrideEvent() {
        return brideEvent;
    }

    public void setBrideEvent(String brideEvent) {
        this.brideEvent = brideEvent;
    }

    public String getBrideName() {
        return brideName;
    }

    public void setBrideName(String brideName) {
        this.brideName = brideName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
