package com.dechub.tanishq.dto.eventsDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class storeCodeDataDTO {

    @NotBlank(message = "Store code is required")
    @Size(max = 50, message = "Store code must not exceed 50 characters")
    private String storeCode;

    @Size(max = 20, message = "Start date must not exceed 20 characters")
    private String startDate;

    @Size(max = 20, message = "End date must not exceed 20 characters")
    private String endDate;

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
