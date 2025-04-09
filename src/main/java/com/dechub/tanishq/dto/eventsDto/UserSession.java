package com.dechub.tanishq.dto.eventsDto;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class UserSession {
    private EventsUser currentUser;


    public EventsUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(EventsUser user) {
        this.currentUser = user;
    }

    public void clear() {
        this.currentUser = null;
    }
}