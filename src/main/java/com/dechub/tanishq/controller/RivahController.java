package com.dechub.tanishq.controller;

import com.dechub.tanishq.dto.rivaahDto.BookAppointmentDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahAllDetailsDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahImagesDTO;
import com.dechub.tanishq.gdrive.GoogleDriveService;
import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
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
    private GoogleDriveService googleServiceUtil;
    @Autowired
    private TanishqPageService tanishqPageService;

    @Autowired
    private GSheetUserDetailsUtil gSheetUserDetailsUtil;

    @GetMapping("/getImages")
    public ResponseEntity<RivaahImagesDTO> getImages(@RequestParam("categories") List<String> tags) throws Exception {
        try {
            RivaahImagesDTO groupedImages = gSheetUserDetailsUtil.getImagesByTags(tags);
            return ResponseEntity.ok(groupedImages);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/increaseLike/{id}")
    public ResponseEntity<String> increaseLike(@PathVariable Long id , @RequestParam boolean increase) throws Exception {

            return gSheetUserDetailsUtil.incrementLikeCount(id,increase);

    }

    @PostMapping("/shareDetails")
    public ResponseEntity<ResponseDataDTO> shareDetails(@RequestBody RivaahDTO rivaahDTO){
        ResponseDataDTO responseDataDTO = tanishqPageService.getShareCode(rivaahDTO);
        if(responseDataDTO.isStatus()){
            return ResponseEntity.ok(responseDataDTO);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDataDTO);
    }

    @PostMapping("/userDetails")
    public ResponseEntity<ResponseDataDTO> storeUser(@RequestParam("name") String name,@RequestParam("contact") String contact){
        ResponseDataDTO responseDataDTO = tanishqPageService.storeRivaahUser(name, contact);
        if(responseDataDTO.isStatus()){
            return ResponseEntity.ok(responseDataDTO);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDataDTO);
    }

    @GetMapping("/getAllDetails/{code}")
    public ResponseEntity<RivaahAllDetailsDTO> getAllDetails(@PathVariable String code){
        RivaahAllDetailsDTO responseDataDTO = tanishqPageService.getRivaahDetails(code);
        if(responseDataDTO.isStatus()){
            return ResponseEntity.ok(responseDataDTO);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDataDTO);
    }

    @PostMapping("/bookAnAppointment")
    public ResponseEntity<ResponseDataDTO> bookAnAppointment(@RequestBody BookAppointmentDTO bookAppointmentDTO) {
        ResponseDataDTO response = tanishqPageService.appointment(bookAppointmentDTO, true);
        return ResponseEntity.ok(response);
    }


}
