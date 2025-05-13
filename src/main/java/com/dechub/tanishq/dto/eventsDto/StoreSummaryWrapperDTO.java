package com.dechub.tanishq.dto.eventsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StoreSummaryWrapperDTO {
    private List<StoreEventSummaryDTO> storeSummaries;
    private StoreEventSummaryDTO totalSummary;
}
