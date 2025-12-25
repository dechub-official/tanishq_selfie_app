package com.dechub.tanishq.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_history")
public class PasswordHistory {

    @Id
    @Column(name = "btq_code", nullable = false)
    private String btqCode;

    @Column(name = "old_password")
    private String oldPassword;

    @Column(name = "new_password", nullable = false)
    private String newPassword;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // Constructors
    public PasswordHistory() {
    }

    public PasswordHistory(String btqCode, String oldPassword, String newPassword, LocalDateTime changedAt) {
        this.btqCode = btqCode;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.changedAt = changedAt;
    }

    // Getters and Setters
    public String getBtqCode() {
        return btqCode;
    }

    public void setBtqCode(String btqCode) {
        this.btqCode = btqCode;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
