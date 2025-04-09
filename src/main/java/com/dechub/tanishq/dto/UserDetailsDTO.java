package com.dechub.tanishq.dto;

public class UserDetailsDTO {

    // private String tellUsYourOccasionForThisPurchase;
    // private String purpose;
    private String name;
    private String reason;
    // private String whatsAppNo;
    private String rsoName;

    private String storeCode;
    // private String emailId;
    private String date;
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
