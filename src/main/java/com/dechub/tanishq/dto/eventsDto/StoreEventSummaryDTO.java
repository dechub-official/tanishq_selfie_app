package com.dechub.tanishq.dto.eventsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StoreEventSummaryDTO {
    private String storeCode;
    private int totalEvents;
    private int totalInvitees;
    private int totalAttendees;
    private double totalAdvance;
    private double totalGhsOrRga;
    private double totalSale;
}
