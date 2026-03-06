package com.dechub.tanishq.dto.rivaahDto;

import com.dechub.tanishq.validation.ValidPhone;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;


@Getter
@Setter
public class BookAppointmentDTO {
    private static final String DEFAULT_TICKET_TYPE = "Rivaah";
    private static final String DEFAULT_BRAND = "Tanishq";

    @JsonProperty("BookingReferenceId")
    @Size(max = 100, message = "Booking reference ID must not exceed 100 characters")
    private String bookingReferenceId;

    @JsonProperty("Message")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    @JsonProperty("Source")
    @Size(max = 100, message = "Source must not exceed 100 characters")
    private String source;

    @JsonProperty("stlocId")
    @Size(max = 50, message = "Store location ID must not exceed 50 characters")
    private String stlocId;

    @JsonProperty("TicketType")
    @Size(max = 50, message = "Ticket type must not exceed 50 characters")
    private String ticketType = DEFAULT_TICKET_TYPE;

    @JsonProperty("Collection")
    @Size(max = 100, message = "Collection must not exceed 100 characters")
    private String collection;

    @JsonProperty("Phone")
    @NotBlank(message = "Phone number is required")
    @ValidPhone
    private String phone;

    @JsonProperty("StoreName")
    @Size(max = 200, message = "Store name must not exceed 200 characters")
    private String storeName;

    @JsonProperty("address")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @JsonProperty("LastName")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]*$", message = "Last name can only contain letters, spaces, and basic punctuation")
    private String lastName;

    @JsonProperty("AppointmentTime")
    @Size(max = 20, message = "Appointment time must not exceed 20 characters")
    private String appointmentTime;

    @JsonProperty("SKU_Code")
    @Size(max = 100, message = "SKU code must not exceed 100 characters")
    private String skuCode;

    @JsonProperty("SKU_Url")
    @Size(max = 500, message = "SKU URL must not exceed 500 characters")
    private String skuUrl;

    @JsonProperty("muid")
    @Size(max = 100, message = "MUID must not exceed 100 characters")
    private String muid;

    @JsonProperty("notificationFlag")
    @Size(max = 10, message = "Notification flag must not exceed 10 characters")
    private String notificationFlag;

    @JsonProperty("Subbrand")
    @Size(max = 100, message = "Subbrand must not exceed 100 characters")
    private String subbrand;

    @JsonProperty("FirstName")
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "First name can only contain letters, spaces, and basic punctuation")
    private String firstName;

    @JsonProperty("EmailId")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String emailId;

    @JsonProperty("Brand")
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    private String brand = DEFAULT_BRAND;

    @JsonProperty("City")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @JsonProperty("AppointmentDate")
    @Size(max = 20, message = "Appointment date must not exceed 20 characters")
    private String appointmentDate;

    @JsonProperty("storeIdentifier")
    @Size(max = 100, message = "Store identifier must not exceed 100 characters")
    private String storeIdentifier;

    @JsonProperty("Store_code")
    @Size(max = 50, message = "Store code must not exceed 50 characters")
    private String storeCode;

    @JsonProperty("VisitorID")
    @Size(max = 100, message = "Visitor ID must not exceed 100 characters")
    private String visitorId;
}
