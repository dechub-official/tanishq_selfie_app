package com.dechub.tanishq.dto.eventsDto;

public class LoginResponseDTO {
    private String userId;
    private String name;

    public LoginResponseDTO(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}

