package com.dechub.tanishq.dto.eventsDto;

import java.util.List;
import java.util.Map;

public class EventsLoginResponseDTO {
    private boolean status;
    private Map<String, Object>  storeData;

    public EventsLoginResponseDTO() {}

    public EventsLoginResponseDTO(boolean status, Map<String, Object>  storeData) {
        this.status = status;
        this.storeData = storeData;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Map<String, Object>  getStoreData() {
        return storeData;
    }

    public void setStoreData(Map<String, Object>  storeData) {
        this.storeData = storeData;
    }
}
