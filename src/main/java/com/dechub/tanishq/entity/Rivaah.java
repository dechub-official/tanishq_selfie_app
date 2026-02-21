package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "rivaah")
public class Rivaah {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // or use code as id
    private Long id;

    private String code; // 10 digit code?
    private String bride;
    private String event;
    private String clothingType;
    private String tags; // comma separated?

    @OneToMany(mappedBy = "rivaah", cascade = CascadeType.ALL)
    private List<ProductDetail> products;

    @OneToMany(mappedBy = "rivaah", cascade = CascadeType.ALL)
    private List<RivaahUser> users;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBride() {
        return bride;
    }

    public void setBride(String bride) {
        this.bride = bride;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getClothingType() {
        return clothingType;
    }

    public void setClothingType(String clothingType) {
        this.clothingType = clothingType;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<ProductDetail> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDetail> products) {
        this.products = products;
    }

    public List<RivaahUser> getUsers() {
        return users;
    }

    public void setUsers(List<RivaahUser> users) {
        this.users = users;
    }
}
