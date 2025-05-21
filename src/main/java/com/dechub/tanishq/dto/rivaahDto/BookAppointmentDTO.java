package com.dechub.tanishq.dto.rivaahDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
//public class BookAppointmentDTO {
//
//    @JsonProperty("TicketType")
//    private String ticketType;
//
//
//    @JsonProperty("Phone")
//    private String phone;
//
//
//    @JsonProperty("LastName")
//    private String lastName;
//
//
//    @JsonProperty("FirstName")
//    private String firstName;
//
//
//    @JsonProperty("Brand")
//    private String brand;
//
//}




public class BookAppointmentDTO {
    private static final String DEFAULT_TICKET_TYPE = "Rivaah";
    private static final String DEFAULT_BRAND = "Tanishq";
    @JsonProperty("BookingReferenceId")
    private String bookingReferenceId;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Source")
    private String source;

    @JsonProperty("stlocId")
    private String stlocId;

    @JsonProperty("TicketType")
    private String ticketType = DEFAULT_TICKET_TYPE;

    @JsonProperty("Collection")
    private String collection;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("StoreName")
    private String storeName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("AppointmentTime")
    private String appointmentTime;

    @JsonProperty("SKU_Code")
    private String skuCode;

    @JsonProperty("SKU_Url")
    private String skuUrl;

    @JsonProperty("muid")
    private String muid;

    @JsonProperty("notificationFlag")
    private String notificationFlag;

    @JsonProperty("Subbrand")
    private String subbrand;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("EmailId")
    private String emailId;

    @JsonProperty("Brand")
    private String brand = DEFAULT_BRAND;

    @JsonProperty("City")
    private String city;

    @JsonProperty("AppointmentDate")
    private String appointmentDate;

    @JsonProperty("storeIdentifier")
    private String storeIdentifier;

    @JsonProperty("Store_code")
    private String storeCode;

    @JsonProperty("VisitorID")
    private String visitorId;
}
