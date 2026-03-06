package com.dechub.tanishq.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserDetailsDTO {

    // private String tellUsYourOccasionForThisPurchase;
    // private String purpose;
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Name can only contain letters, spaces, and basic punctuation")
    private String name;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    // private String whatsAppNo;

    @Size(max = 100, message = "RSO name must not exceed 100 characters")
    private String rsoName;

    @NotBlank(message = "Store code is required")
    @Size(max = 50, message = "Store code must not exceed 50 characters")
    private String storeCode;
    // private String emailId;

    @NotBlank(message = "Date is required")
    @Size(max = 20, message = "Date must not exceed 20 characters")
    private String date;

    @Size(max = 50, message = "My first diamond field must not exceed 50 characters")
    private String myFirstDiamond;
    // private String cityName;
    // private String selfieImageName;

    public String getRsoName() {
        return rsoName;
    }

    public void setRsoName(String rsoName) {
        this.rsoName = rsoName;
    }

    public String getMyFirstDiamond() {
        return myFirstDiamond;
    }

    public void setMyFirstDiamond(String myFirstDiamond) {
        this.myFirstDiamond = myFirstDiamond;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // public String getEmailId() {
    //     return emailId;
    // }

    // public void setEmailId(String emailId) {
    //     this.emailId = emailId;
    // }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // public String getSelfieImageName() {
    //     return selfieImageName;
    // }

    // public void setSelfieImageName(String selfieImageName) {
    //     this.selfieImageName = selfieImageName;
    // }
}
