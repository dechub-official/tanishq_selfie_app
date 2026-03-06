package com.dechub.tanishq.dto.eventsDto;

import com.dechub.tanishq.validation.ValidPhone;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttendeesDetailDTO {

    @Size(max = 50, message = "ID must not exceed 50 characters")
    private String id;

    @Size(max = 50, message = "Event ID must not exceed 50 characters")
    private String eventId;

    @Size(max = 100, message = "Event type must not exceed 100 characters")
    private String eventType;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Name can only contain letters, spaces, and basic punctuation")
    private String name;

    @Size(max = 50, message = "Store code must not exceed 50 characters")
    private String storeCode;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    @Size(max = 100, message = "RSO name must not exceed 100 characters")
    private String rsoName;

    @NotBlank(message = "Phone number is required")
    @ValidPhone
    private String phone;

    @Size(max = 500, message = "Like field must not exceed 500 characters")
    private String like;

    @Size(max = 50, message = "Created at must not exceed 50 characters")
    private String createdAt;

    private boolean firstTimeAtTanishq;

    private MultipartFile file;

    private boolean bulkUpload;
    private boolean fromQrCode;

}
