package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "greetings")
public class Greeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uniqueId;
    private String greetingText; // like "name"
    private String phone;
    private String message;
    private String qrCodeData;
    private String driveFileId;
    private LocalDateTime createdAt;
    private Boolean uploaded;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getGreetingText() {
        return greetingText;
    }

    public void setGreetingText(String greetingText) {
        this.greetingText = greetingText;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public String getDriveFileId() {
        return driveFileId;
    }

    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }
}
