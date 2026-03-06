package com.dechub.tanishq.controller;


import com.dechub.tanishq.dto.BrideDetailsDTO;
import com.dechub.tanishq.dto.UserDetailsDTO;
import com.dechub.tanishq.service.TanishqPageService;
import com.dechub.tanishq.util.APIResponseBuilder;
import com.dechub.tanishq.util.InputValidator;
import com.dechub.tanishq.util.ResponseDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("tanishq/selfie")
public class TanishqPageController {

    @Autowired
    private TanishqPageService tanishqPageService;


    @PostMapping(path = "save", produces = "application/json")
    public ResponseEntity<ResponseDataDTO> storeUserDetails(@Valid @RequestBody UserDetailsDTO userDetailsDTO) {
        return APIResponseBuilder.buildResponseFromDto(tanishqPageService.storeUserDetails(userDetailsDTO));
    }


    @PostMapping(value = "upload")
//    @GetMapping(value = "upload")
    public ResponseEntity<ResponseDataDTO> uploadImage(@RequestParam("selfie") MultipartFile file, @RequestParam("storeCode") String storeCode) throws IOException {
        // INPUT VALIDATION: Validate store code
        if (storeCode == null || storeCode.trim().isEmpty()) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Store code is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (!InputValidator.isValidStoreCode(storeCode)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Invalid store code format");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (file == null || file.isEmpty()) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Image file is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        return APIResponseBuilder.buildResponseFromDto(tanishqPageService.saveImage(file, storeCode));
    }

    @GetMapping(value = "getStoreCode")
    public ResponseEntity<ResponseDataDTO> getStoreCode() throws IOException {
        return APIResponseBuilder.buildResponseFromDto(tanishqPageService.getStoreCode());
    }
    @PostMapping("/brideImage")
    public ResponseEntity<ResponseDataDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        // INPUT VALIDATION: Validate file is present
        if (file == null || file.isEmpty()) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Image file is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        return APIResponseBuilder.buildResponseFromDto(tanishqPageService.saveBrideImage(file));
    }

    @PostMapping("/brideDetails")
    public ResponseEntity<byte[]> storeBrideDetails(@Valid @RequestBody BrideDetailsDTO brideDetailsDTO) {
        return tanishqPageService.storeBrideDetails(
            brideDetailsDTO.getBrideType(),
            brideDetailsDTO.getBrideEvent(),
            brideDetailsDTO.getBrideName(),
            brideDetailsDTO.getPhone(),
            brideDetailsDTO.getDate(),
            brideDetailsDTO.getEmail(),
            brideDetailsDTO.getZipCode(),
            null
        );
    }
    
}
