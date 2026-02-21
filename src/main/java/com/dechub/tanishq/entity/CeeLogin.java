package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cee_login")
public class CeeLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cee_user_id", unique = true, nullable = false)
    private String ceeUserId;

    @Column(name = "cee_name")
    private String ceeName;

    private String password;
    private String email;
    private String region;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCeeUserId() {
        return ceeUserId;
    }

    public void setCeeUserId(String ceeUserId) {
        this.ceeUserId = ceeUserId;
    }

    public String getCeeName() {
        return ceeName;
    }

    public void setCeeName(String ceeName) {
        this.ceeName = ceeName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
