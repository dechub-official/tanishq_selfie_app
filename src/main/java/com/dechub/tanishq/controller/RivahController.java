package com.dechub.tanishq.controller;

import com.dechub.tanishq.dto.rivaahDto.BookAppointmentDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahAllDetailsDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahImagesDTO;
import com.dechub.tanishq.service.TanishqPageService;
import com.dechub.tanishq.util.ResponseDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/rivaah")
public class RivahController {
    @Autowired
    private TanishqPageService tanishqPageService;

    @GetMapping("/getImages")
    public ResponseEntity<RivaahImagesDTO> getImages(@RequestParam("categories") List<String> tags) throws Exception {
        // Rivaah features disabled during Google Sheets to MySQL migration
        return ResponseEntity.status(503).body(new RivaahImagesDTO()); // Service Unavailable
    }

    @GetMapping("/increaseLike/{id}")
    public ResponseEntity<String> increaseLike(@PathVariable Long id , @RequestParam boolean increase) throws Exception {
        return ResponseEntity.status(503).body("Rivaah like feature temporarily disabled - system upgrade in progress");
    }

    @PostMapping("/shareDetails")
    public ResponseEntity<ResponseDataDTO> shareDetails(@RequestBody RivaahDTO rivaahDTO){
        // Stub - Rivaah sharing disabled during migration
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        responseDataDTO.setStatus(false);
        responseDataDTO.setMessage("Rivaah sharing temporarily disabled");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDataDTO);
    }

    @PostMapping("/userDetails")
    public ResponseEntity<ResponseDataDTO> storeUser(@RequestParam("name") String name,@RequestParam("contact") String contact){
        // Stub - Rivaah user storage disabled during migration
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();
        responseDataDTO.setStatus(false);
        responseDataDTO.setMessage("Rivaah user storage temporarily disabled");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDataDTO);
    }

    @GetMapping("/getAllDetails/{code}")
    public ResponseEntity<RivaahAllDetailsDTO> getAllDetails(@PathVariable String code){
        // Stub - Rivaah details disabled during migration
        RivaahAllDetailsDTO responseDataDTO = new RivaahAllDetailsDTO();
        responseDataDTO.setStatus(false);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDataDTO);
    }

    @PostMapping("/bookAnAppointment")
    public ResponseEntity<ResponseDataDTO> bookAnAppointment(@RequestBody BookAppointmentDTO bookAppointmentDTO) {
        // Use QrCodeService - Rivaah appointment disabled during migration
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(false);
        dto.setMessage("Rivaah appointment booking temporarily disabled");
        return ResponseEntity.ok(dto);
    }


}
