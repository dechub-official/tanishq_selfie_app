package com.dechub.tanishq.dto.rivaahDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RivaahImagesDTO {
    private List<String> categories;
    private List<ProductDetailDTO> productDetails;


}
