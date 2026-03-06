package com.dechub.tanishq.dto;

import com.dechub.tanishq.validation.ValidPhone;

import javax.validation.constraints.*;

public class BrideDetailsDTO {

    @NotBlank(message = "Bride type is required")
    @Size(max = 50, message = "Bride type must not exceed 50 characters")
    private String brideType;

    @NotBlank(message = "Bride event is required")
    @Size(max = 100, message = "Bride event must not exceed 100 characters")
    private String brideEvent;

    @NotBlank(message = "Bride name is required")
    @Size(min = 2, max = 100, message = "Bride name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Bride name can only contain letters, spaces, and basic punctuation")
    private String brideName;

    @NotBlank(message = "Phone number is required")
    @ValidPhone
    private String phone;

    @NotBlank(message = "Date is required")
    @Size(max = 20, message = "Date must not exceed 20 characters")
    private String date;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Zip code must be exactly 6 digits")
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
