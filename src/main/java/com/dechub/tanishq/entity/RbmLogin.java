package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rbm_login")
public class RbmLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rbm_user_id", unique = true, nullable = false)
    private String rbmUserId;

    @Column(name = "rbm_name")
    private String rbmName;

    private String password;
    private String email;

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

    public String getRbmUserId() {
        return rbmUserId;
    }

    public void setRbmUserId(String rbmUserId) {
        this.rbmUserId = rbmUserId;
    }

    public String getRbmName() {
        return rbmName;
    }

    public void setRbmName(String rbmName) {
        this.rbmName = rbmName;
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
