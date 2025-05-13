package com.dechub.tanishq.dto.eventsDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttendeesDetailDTO {
    private String id;
    private String eventId;
    private String eventType;
    private String name;
    private String storeCode;
    private String region;
    private String rsoName;
    private String phone;
    private String like;
    private String createdAt;
    private boolean firstTimeAtTanishq;

    private MultipartFile file;

}
