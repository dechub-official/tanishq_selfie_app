package com.dechub.tanishq.controller;


import com.dechub.tanishq.dto.UserDetailsDTO;
import com.dechub.tanishq.service.TanishqPageService;
import com.dechub.tanishq.util.APIResponseBuilder;
import com.dechub.tanishq.util.ResponseDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@RestController
@RequestMapping("tanishq/selfie")
public class TanishqPageController {

    @Autowired
    private TanishqPageService tanishqPageService;


    @PostMapping(path = "save", produces = "application/json")
    public ResponseEntity<ResponseDataDTO> storeUserDetails(@RequestBody UserDetailsDTO userDetailsDTO) {
        return APIResponseBuilder.buildResponseFromDto(tanishqPageService.storeUserDetails(userDetailsDTO));
    }


    @PostMapping(value = "upload")
//    @GetMapping(value = "upload")
    public ResponseEntity<ResponseDataDTO> uploadImage(@RequestParam("selfie") MultipartFile file, @RequestParam("storeCode") String storeCode) throws IOException {
        return APIResponseBuilder.buildResponseFromDto(tanishqPageService.saveImage(file, storeCode));
    }

    @GetMapping(value = "getStoreCode")
    public ResponseEntity<ResponseDataDTO> getStoreCode() throws IOException {
        return APIResponseBuilder.buildResponseFromDto(tanishqPageService.getStoreCode());
    }
    @PostMapping("/brideImage")
    public ResponseEntity<ResponseDataDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        return APIResponseBuilder.buildResponseFromDto(tanishqPageService.storeBrideImage(file));
    }

    @PostMapping("/brideDetails")
    public ResponseEntity<byte[]> storeBrideDetails(
            @RequestParam("brideType") String brideType,
            @RequestParam("brideEvent") String brideEvent,
            @RequestParam("brideName") String brideName,
                @RequestParam("phone") String phone,
            @RequestParam("date") String date,
            @RequestParam("email") String email,
            @RequestParam("zipCode") String zipCode,
                @RequestParam("filepath") String filepath

    ) {
        return tanishqPageService.storeBrideDetails(brideType, brideEvent, brideName, phone, date, email,zipCode,filepath);
    }
    
}
