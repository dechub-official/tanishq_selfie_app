package com.dechub.tanishq.dto.eventsDto;

public class EventsUser {
    private String userStoreCode;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventsUser(String userStoreCode) {
        this.userStoreCode = userStoreCode;
    }

    public String getUserStoreCode() {
        return userStoreCode;
    }

    public void setUserStoreCode(String userStoreCode) {
        this.userStoreCode = userStoreCode;
    }
}
