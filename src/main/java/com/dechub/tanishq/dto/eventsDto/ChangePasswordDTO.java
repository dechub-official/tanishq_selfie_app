package com.dechub.tanishq.dto.eventsDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for password change requests
 */
public class ChangePasswordDTO {

    @NotBlank(message = "Store code is required")
    @Size(max = 50, message = "Store code must not exceed 50 characters")
    private String storeCode;

    @NotBlank(message = "Old password is required")
    @Size(min = 4, max = 100, message = "Old password must be between 4 and 100 characters")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 4, max = 100, message = "New password must be between 4 and 100 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 4, max = 100, message = "Confirm password must be between 4 and 100 characters")
    private String confirmPassword;

    // Getters and setters
    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

