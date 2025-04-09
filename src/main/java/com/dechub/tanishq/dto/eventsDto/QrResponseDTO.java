package com.dechub.tanishq.dto.eventsDto;

public class QrResponseDTO {
    private boolean status;
    private String qrData;

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getQrData() {
        return qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }
}
