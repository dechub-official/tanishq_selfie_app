package com.dechub.tanishq.controller;

import com.dechub.tanishq.dto.qrcode.GreetingInfo;
import com.dechub.tanishq.service.qrservices.GreetingService;
import com.dechub.tanishq.service.qrservices.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/greetings")
public class GreetingController {

    @Autowired
    private GreetingService greetingService;

    @Autowired
    private QrCodeService qrCodeService;

    /**
     * THIS IS THE NEW ENDPOINT FOR THE VIEW FEATURE
     * It checks if a video has been uploaded for a given ID.
     */

    @PostMapping("/generate")
    public ResponseEntity<String> generateGreetingLink() {
        try {
            String uniqueId = greetingService.createGreeting();
            // return ONLY the id
            return ResponseEntity.ok(uniqueId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to generate id: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{uniqueId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateQrCode(@PathVariable String uniqueId) throws Exception {
        // encode ONLY the id inside the QR
        return qrCodeService.generateQrCodeImage(uniqueId, 300, 300);
    }
    @PostMapping(path = "/{uniqueId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadVideo(
            @PathVariable String uniqueId,
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "message", required = false) String message) {
        try {
            greetingService.uploadVideoAndUpdateGreeting(uniqueId, videoFile, name, message);
            return ResponseEntity.ok("Video uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload video: " + e.getMessage());
        }
    }


    @GetMapping("/{uniqueId}/view")
    public ResponseEntity<?> getGreetingInfo(@PathVariable String uniqueId) {
        try {
            Optional<GreetingInfo> greetingInfo = greetingService.getGreetingInfo(uniqueId);
            if (greetingInfo.isPresent()) {
                return ResponseEntity.ok(greetingInfo.get());
            } else {
                return ResponseEntity.status(404).body("Greeting not found with this ID.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving greeting information: " + e.getMessage());
        }
    }
}