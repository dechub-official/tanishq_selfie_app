package com.dechub.tanishq.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.millij.poi.ss.model.annotations.Sheet;
import io.github.millij.poi.ss.model.annotations.SheetColumn;

@Sheet("Tanishq Stores - Updated")
public class ExcelStoreDTO {

    @JsonIgnoreProperties(value = {
            "storeManagerName",
            "storeManagerNo",
            "storeManagerEmail"
    })

    @SheetColumn("Btq Code")
    public String storeCode;

    @SheetColumn("Btq Name")
    public String storeName;

    @SheetColumn("Btq Address")
    public String storeAddress;
    @SheetColumn("City")
    public String storeCity;

    @SheetColumn("State")
    public String storeState;

    @SheetColumn("Country")
    public String storeCountry;

    @SheetColumn("Zipcode")
    public String storeZipCode;

    @SheetColumn("Btq Phone 1 (Primary)")
    public String storePhoneNoOne;

    @SheetColumn("Btq Phone 2")
    public String storePhoneNoTwo;

    @SheetColumn("Btq Email id")
    public String storeEmailId;

    @SheetColumn("Lattitude")
    public String storeLatitude;


    @SheetColumn("Longitude")
    public String storeLongitude;

    @SheetColumn("Date of Opening")
    public String storeDateOfOpening;

    @SheetColumn("Store Type")
    public String storeType;

    @SheetColumn("Store Opening Time")
    public String storeOpeningTime;

    @SheetColumn("Store Closing Time")
    public String storeClosingTime;

    @SheetColumn("Store Manager Name")
    @JsonIgnore
    public String storeManagerName;

    @SheetColumn("Str Manager No.")
    @JsonIgnore
    public String storeManagerNo;

    @SheetColumn("Store Manager Email")
    @JsonIgnore
    public String storeManagerEmail;

    @SheetColumn("Bitly Link")
    public String storeLocationLink;

    @SheetColumn("Languages")
    public Object languages;

    @SheetColumn("Parking")
    public Object parking;

    @SheetColumn("Payment")
    public Object payment;

    @SheetColumn("Kakatiya_store")
    public String kakatiyaStore;

    @SheetColumn("Celeste_store")
    public String celesteStore;

    @SheetColumn("rating")
    public String rating;

    @SheetColumn("number_of_ratings")
    public String numberOfRatings;

    @SheetColumn("isCollection")
    public String isCollection;

    public String getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(String isCollection) {
        this.isCollection = isCollection;
    }

    public String getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(String numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }


    public String getCelesteStore() {
        return celesteStore;
    }

    public void setCelesteStore(String celesteStore) {
        this.celesteStore = celesteStore;
    }

    public String getKakatiyaStore() {
        return kakatiyaStore;
    }

    public void setKakatiyaStore(String kakatiyaStore) {
        this.kakatiyaStore = kakatiyaStore;
    }

    public Object getLanguages() {
        return languages;
    }

    public void setLanguages(Object languages) {
        this.languages = languages;
    }

    public Object getParking() {
        return parking;
    }

    public void setParking(Object parking) {
        this.parking = parking;
    }

    public Object getPayment() {
        return payment;
    }

    public void setPayment(Object payment) {
        this.payment = payment;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStoreCity() {
        return storeCity;
    }

    public void setStoreCity(String storeCity) {
        this.storeCity = storeCity;
    }

    public String getStoreState() {
        return storeState;
    }

    public void setStoreState(String storeState) {
        this.storeState = storeState;
    }

    public String getStoreCountry() {
        return storeCountry;
    }

    public void setStoreCountry(String storeCountry) {
        this.storeCountry = storeCountry;
    }

    public String getStoreZipCode() {
        return storeZipCode;
    }

    public void setStoreZipCode(String storeZipCode) {
        this.storeZipCode = storeZipCode;
    }

    public void setStoreManagerNo(String storeManagerNo) {
        this.storeManagerNo = storeManagerNo;
    }

    public String getStorePhoneNoOne() {
        return storePhoneNoOne;
    }

    public void setStorePhoneNoOne(String storePhoneNoOne) {
        this.storePhoneNoOne = storePhoneNoOne;
    }

    public String getStorePhoneNoTwo() {
        return storePhoneNoTwo;
    }

    public void setStorePhoneNoTwo(String storePhoneNoTwo) {
        this.storePhoneNoTwo = storePhoneNoTwo;
    }

    public String getStoreEmailId() {
        return storeEmailId;
    }

    public void setStoreEmailId(String storeEmailId) {
        this.storeEmailId = storeEmailId;
    }

    public String getStoreLatitude() {
        return storeLatitude;
    }

    public void setStoreLatitude(String storeLatitude) {
        this.storeLatitude = storeLatitude;
    }

    public String getStoreLongitude() {
        return storeLongitude;
    }

    public void setStoreLongitude(String storeLongitude) {
        this.storeLongitude = storeLongitude;
    }

    public String getStoreDateOfOpening() {
        return storeDateOfOpening;
    }

    public void setStoreDateOfOpening(String storeDateOfOpening) {
        this.storeDateOfOpening = storeDateOfOpening;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getStoreOpeningTime() {
        return storeOpeningTime;
    }

    public void setStoreOpeningTime(String storeOpeningTime) {
        this.storeOpeningTime = storeOpeningTime;
    }

    public String getStoreClosingTime() {
        return storeClosingTime;
    }

    public void setStoreClosingTime(String storeClosingTime) {
        this.storeClosingTime = storeClosingTime;
    }

    public String getStoreManagerName() {
        return storeManagerName;
    }

    public void setStoreManagerName(String storeManagerName) {
        this.storeManagerName = storeManagerName;
    }

    public String getStoreManagerNo() {
        return storeManagerNo;
    }

    public String getStoreManagerEmail() {
        return storeManagerEmail;
    }

    public void setStoreManagerEmail(String storeManagerEmail) {
        this.storeManagerEmail = storeManagerEmail;
    }

    public String getStoreLocationLink() {
        return storeLocationLink;
    }

    public void setStoreLocationLink(String storeLocationLink) {
        this.storeLocationLink = storeLocationLink;
    }

    @Override
    public String toString() {
        return "ExcelStoreDTO{" +
                "storeCode='" + storeCode + '\'' +
                ", storeName='" + storeName + '\'' +
                ", storeAddress='" + storeAddress + '\'' +
                ", storeCity='" + storeCity + '\'' +
                ", storeState='" + storeState + '\'' +
                ", storeCountry='" + storeCountry + '\'' +
                ", storeZipCode='" + storeZipCode + '\'' +
                ", storePhoneNoOne='" + storePhoneNoOne + '\'' +
                ", storePhoneNoTwo='" + storePhoneNoTwo + '\'' +
                ", storeEmailId='" + storeEmailId + '\'' +
                ", storeLatitude='" + storeLatitude + '\'' +
                ", storeLongitude='" + storeLongitude + '\'' +
                ", storeDateOfOpening='" + storeDateOfOpening + '\'' +
                ", storeType='" + storeType + '\'' +
                ", storeOpeningTime='" + storeOpeningTime + '\'' +
                ", storeClosingTime='" + storeClosingTime + '\'' +
                ", storeManagerName='" + storeManagerName + '\'' +
                ", storeManagerNo='" + storeManagerNo + '\'' +
                ", storeManagerEmail='" + storeManagerEmail + '\'' +
                ", storeLocationLink='" + storeLocationLink + '\'' +
                ", languages=" + languages +
                ", parking=" + parking +
                ", payment=" + payment +
                ", kakatiyaStore='" + kakatiyaStore + '\'' +
                ", celesteStore='" + celesteStore + '\'' +
                ", rating='" + rating + '\'' +
                ", numberOfRatings='" + numberOfRatings + '\'' +
                '}';
    }
}
