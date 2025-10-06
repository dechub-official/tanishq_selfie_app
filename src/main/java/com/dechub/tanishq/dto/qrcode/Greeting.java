package com.dechub.tanishq.dto.qrcode;

import java.util.UUID;

// We have removed @Data and are adding the methods ourselves.
public class Greeting {

    private String uniqueId = UUID.randomUUID().toString();
    private String googleDriveFileId;

    // Getter for uniqueId
    public String getUniqueId() {
        return uniqueId;
    }

    // Getter for googleDriveFileId
    public String getGoogleDriveFileId() {
        return googleDriveFileId;
    }

    // Setter for googleDriveFileId (This is the method the compiler couldn't find)
    public void setGoogleDriveFileId(String googleDriveFileId) {

        this.googleDriveFileId = googleDriveFileId;
    }
}