package com.dechub.tanishq.dto.rivaahDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RivaahAllDetailsDTO
{
    private boolean status;
    private String message;
    private String code;
    private String bride;
    private String event;
    private String clothing_type;
    private List<String> tags;
    private RivaahImagesDTO images;

}
