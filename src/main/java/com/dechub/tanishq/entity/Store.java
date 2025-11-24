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
    private String languages;   // kept as String for now (comma separated)
    private String parking;     // same
    private String payment;     // same
    private String kakatiyaStore;
    private String celesteStore;
    private String rating;
    private String numberOfRatings;
    private String isCollection;

    // 🔥 IMPORTANT
    private String region;
    private String level;// e.g. "North1", "South2", "East1"

    // Managers/Regional
    private String abmUsername;    // e.g. "ABM_NORTH1"
    private String rbmUsername;    // e.g. "RBM_NORTH"
    private String ceeUsername;    // e.g. "CEE_NORTH"

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Event> events;

    // === getters/setters ===

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }

    public String getStoreCity() { return storeCity; }
    public void setStoreCity(String storeCity) { this.storeCity = storeCity; }

    public String getStoreState() { return storeState; }
    public void setStoreState(String storeState) { this.storeState = storeState; }

    public String getStoreCountry() { return storeCountry; }
    public void setStoreCountry(String storeCountry) { this.storeCountry = storeCountry; }

    public String getStoreZipCode() { return storeZipCode; }
    public void setStoreZipCode(String storeZipCode) { this.storeZipCode = storeZipCode; }

    public String getStorePhoneNoOne() { return storePhoneNoOne; }
    public void setStorePhoneNoOne(String storePhoneNoOne) { this.storePhoneNoOne = storePhoneNoOne; }

    public String getStorePhoneNoTwo() { return storePhoneNoTwo; }
    public void setStorePhoneNoTwo(String storePhoneNoTwo) { this.storePhoneNoTwo = storePhoneNoTwo; }

    public String getStoreEmailId() { return storeEmailId; }
    public void setStoreEmailId(String storeEmailId) { this.storeEmailId = storeEmailId; }

    public String getStoreLatitude() { return storeLatitude; }
    public void setStoreLatitude(String storeLatitude) { this.storeLatitude = storeLatitude; }

    public String getStoreLongitude() { return storeLongitude; }
    public void setStoreLongitude(String storeLongitude) { this.storeLongitude = storeLongitude; }

    public String getStoreDateOfOpening() { return storeDateOfOpening; }
    public void setStoreDateOfOpening(String storeDateOfOpening) { this.storeDateOfOpening = storeDateOfOpening; }

    public String getStoreType() { return storeType; }
    public void setStoreType(String storeType) { this.storeType = storeType; }

    public String getStoreOpeningTime() { return storeOpeningTime; }
    public void setStoreOpeningTime(String storeOpeningTime) { this.storeOpeningTime = storeOpeningTime; }

    public String getStoreClosingTime() { return storeClosingTime; }
    public void setStoreClosingTime(String storeClosingTime) { this.storeClosingTime = storeClosingTime; }

    public String getStoreManagerName() { return storeManagerName; }
    public void setStoreManagerName(String storeManagerName) { this.storeManagerName = storeManagerName; }

    public String getStoreManagerNo() { return storeManagerNo; }
    public void setStoreManagerNo(String storeManagerNo) { this.storeManagerNo = storeManagerNo; }

    public String getStoreManagerEmail() { return storeManagerEmail; }
    public void setStoreManagerEmail(String storeManagerEmail) { this.storeManagerEmail = storeManagerEmail; }

    public String getStoreLocationLink() { return storeLocationLink; }
    public void setStoreLocationLink(String storeLocationLink) { this.storeLocationLink = storeLocationLink; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public String getParking() { return parking; }
    public void setParking(String parking) { this.parking = parking; }

    public String getPayment() { return payment; }
    public void setPayment(String payment) { this.payment = payment; }

    public String getKakatiyaStore() { return kakatiyaStore; }
    public void setKakatiyaStore(String kakatiyaStore) { this.kakatiyaStore = kakatiyaStore; }

    public String getCelesteStore() { return celesteStore; }
    public void setCelesteStore(String celesteStore) { this.celesteStore = celesteStore; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getNumberOfRatings() { return numberOfRatings; }
    public void setNumberOfRatings(String numberOfRatings) { this.numberOfRatings = numberOfRatings; }

    public String getIsCollection() { return isCollection; }
    public void setIsCollection(String isCollection) { this.isCollection = isCollection; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = events; }

    public String getAbmUsername() { return abmUsername; }
    public void setAbmUsername(String abmUsername) { this.abmUsername = abmUsername; }

    public String getRbmUsername() { return rbmUsername; }
    public void setRbmUsername(String rbmUsername) { this.rbmUsername = rbmUsername; }

    public String getCeeUsername() { return ceeUsername; }
    public void setCeeUsername(String ceeUsername) { this.ceeUsername = ceeUsername; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
