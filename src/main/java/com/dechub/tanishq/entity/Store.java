package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    private String storeCode;

    private String storeName;
    private String storeAddress;
    private String storeCity;
    private String storeState;
    private String storeCountry;
    private String storeZipCode;
    private String storePhoneNoOne;
    private String storePhoneNoTwo;
    private String storeEmailId;
    private String storeLatitude;
    private String storeLongitude;
    private String storeDateOfOpening;
    private String storeType;
    private String storeOpeningTime;
    private String storeClosingTime;
    private String storeManagerName;
    private String storeManagerNo;
    private String storeManagerEmail;
    private String storeLocationLink;
    private String languages; // or List<String>
    private String parking; // or List<String>
    private String payment; // or List<String>
    private String kakatiyaStore;
    private String celesteStore;
    private String rating;
    private String numberOfRatings;
    private String isCollection;

    // 🔥 IMPORTANT
    private String region;    // e.g. "North1", "South2", "East1"
    private String level;     // Store level/tier

    // Managers/Regional
    private String abmUsername;
    private String rbmUsername;
    private String ceeUsername;
    private String corporateUsername;  // Top-level corporate access

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Event> events;

    // getters and setters

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

    public String getStoreEmailId() {
        return storeEmailId;
    }

    public void setStoreEmailId(String storeEmailId) {
        this.storeEmailId = storeEmailId;
    }

    public String getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(String isCollection) {
        this.isCollection = isCollection;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAbmUsername() {
        return abmUsername;
    }

    public String setAbmUsername(String abmUsername) {
        return this.abmUsername = abmUsername;
    }

    public String getRbmUsername() {
        return rbmUsername;
    }

    public String setRbmUsername(String rbmUsername) {
        return this.rbmUsername = rbmUsername;
    }

    public String getCeeUsername() {
        return ceeUsername;
    }

    public String setCeeUsername(String ceeUsername) {
        return this.ceeUsername = ceeUsername;
    }

    public String getCorporateUsername() {
        return corporateUsername;
    }

    public String setCorporateUsername(String corporateUsername) {
        return this.corporateUsername = corporateUsername;
    }
}
