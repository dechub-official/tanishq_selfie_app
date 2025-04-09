package com.dechub.tanishq.dto.eventsDto;




import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.millij.poi.ss.model.annotations.Sheet;
import io.github.millij.poi.ss.model.annotations.SheetColumn;

import java.util.ArrayList;


public class    StoreDetailsDTO {

    public String BtqCode;

    public String BtqName;


    public String BtqAddress;
    public String BtqCity;

    public String BtqState;

    public String BtqCountry;

    public String BtqZipCode;

    public String BtqPhoneNoOne;

    public String BtqPhoneNoTwo;

    public String BtqEmailId;

    public String BtqLatitude;

    public String BtqLongitude;

    public String BtqDateOfOpening;

    public String BtqType;

    public String BtqOpeningTime;

    public String BtqClosingTime;

    public String BtqManagerName;

    public String BtqManagerNo;

    public String BtqManagerEmail;

    public String BtqLocationLink;

    public Object languages;

    public Object parking;

    public Object payment;

    public String kakatiyaStore;

    public String celesteStore;

    public String rating;

    public String numberOfRatings;

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
        return BtqCode;
    }

    public void setStoreCode(String BtqCode) {
        this.BtqCode = BtqCode;
    }

    public String getStoreName() {
        return BtqName;
    }

    public void setStoreName(String BtqName) {
        this.BtqName = BtqName;
    }

    public String getStoreAddress() {
        return BtqAddress;
    }

    public void setStoreAddress(String BtqAddress) {
        this.BtqAddress = BtqAddress;
    }

    public String getStoreCity() {
        return BtqCity;
    }

    public void setStoreCity(String BtqCity) {
        this.BtqCity = BtqCity;
    }

    public String getStoreState() {
        return BtqState;
    }

    public void setStoreState(String BtqState) {
        this.BtqState = BtqState;
    }

    public String getStoreCountry() {
        return BtqCountry;
    }

    public void setStoreCountry(String BtqCountry) {
        this.BtqCountry = BtqCountry;
    }

    public String getStoreZipCode() {
        return BtqZipCode;
    }

    public void setStoreZipCode(String BtqZipCode) {
        this.BtqZipCode = BtqZipCode;
    }

    public void setStoreManagerNo(String BtqManagerNo) {
        this.BtqManagerNo = BtqManagerNo;
    }

    public String getStorePhoneNoOne() {
        return BtqPhoneNoOne;
    }

    public void setStorePhoneNoOne(String BtqPhoneNoOne) {
        this.BtqPhoneNoOne = BtqPhoneNoOne;
    }

    public String getStorePhoneNoTwo() {
        return BtqPhoneNoTwo;
    }

    public void setStorePhoneNoTwo(String BtqPhoneNoTwo) {
        this.BtqPhoneNoTwo = BtqPhoneNoTwo;
    }

    public String getStoreEmailId() {
        return BtqEmailId;
    }

    public void setStoreEmailId(String BtqEmailId) {
        this.BtqEmailId = BtqEmailId;
    }

    public String getStoreLatitude() {
        return BtqLatitude;
    }

    public void setStoreLatitude(String BtqLatitude) {
        this.BtqLatitude = BtqLatitude;
    }

    public String getStoreLongitude() {
        return BtqLongitude;
    }

    public void setStoreLongitude(String BtqLongitude) {
        this.BtqLongitude = BtqLongitude;
    }

    public String getStoreDateOfOpening() {
        return BtqDateOfOpening;
    }

    public void setStoreDateOfOpening(String BtqDateOfOpening) {
        this.BtqDateOfOpening = BtqDateOfOpening;
    }

    public String getStoreType() {
        return BtqType;
    }

    public void setStoreType(String BtqType) {
        this.BtqType = BtqType;
    }

    public String getStoreOpeningTime() {
        return BtqOpeningTime;
    }

    public void setStoreOpeningTime(String BtqOpeningTime) {
        this.BtqOpeningTime = BtqOpeningTime;
    }

    public String getStoreClosingTime() {
        return BtqClosingTime;
    }

    public void setStoreClosingTime(String BtqClosingTime) {
        this.BtqClosingTime = BtqClosingTime;
    }

    public String getStoreManagerName() {
        return BtqManagerName;
    }

    public void setStoreManagerName(String BtqManagerName) {
        this.BtqManagerName = BtqManagerName;
    }

    public String getStoreManagerNo() {
        return BtqManagerNo;
    }

    public String getStoreManagerEmail() {
        return BtqManagerEmail;
    }

    public void setStoreManagerEmail(String BtqManagerEmail) {
        this.BtqManagerEmail = BtqManagerEmail;
    }

    public String getStoreLocationLink() {
        return BtqLocationLink;
    }

    public void setStoreLocationLink(String BtqLocationLink) {
        this.BtqLocationLink = BtqLocationLink;
    }

    @Override
    public String toString() {
        return "ExcelStoreDTO{" +
                "BtqCode='" + BtqCode + '\'' +
                ", BtqName='" + BtqName + '\'' +
                ", BtqAddress='" + BtqAddress + '\'' +
                ", BtqCity='" + BtqCity + '\'' +
                ", BtqState='" + BtqState + '\'' +
                ", BtqCountry='" + BtqCountry + '\'' +
                ", BtqZipCode='" + BtqZipCode + '\'' +
                ", BtqPhoneNoOne='" + BtqPhoneNoOne + '\'' +
                ", BtqPhoneNoTwo='" + BtqPhoneNoTwo + '\'' +
                ", BtqEmailId='" + BtqEmailId + '\'' +
                ", BtqLatitude='" + BtqLatitude + '\'' +
                ", BtqLongitude='" + BtqLongitude + '\'' +
                ", BtqDateOfOpening='" + BtqDateOfOpening + '\'' +
                ", BtqType='" + BtqType + '\'' +
                ", BtqOpeningTime='" + BtqOpeningTime + '\'' +
                ", BtqClosingTime='" + BtqClosingTime + '\'' +
                ", BtqManagerName='" + BtqManagerName + '\'' +
                ", BtqManagerNo='" + BtqManagerNo + '\'' +
                ", BtqManagerEmail='" + BtqManagerEmail + '\'' +
                ", BtqLocationLink='" + BtqLocationLink + '\'' +
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
