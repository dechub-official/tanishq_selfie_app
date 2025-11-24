package com.dechub.tanishq.controller;

import com.dechub.tanishq.dto.qrcode.GreetingInfo;
import com.dechub.tanishq.service.TanishqPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/greetings")
public class GreetingController {

    // Stub services for now (services deleted during migration)
    // @Autowired
    // private GreetingService greetingService;

    // @Autowired
    // private QrCodeService qrCodeService;

    /**
     * THIS IS THE NEW ENDPOINT FOR THE VIEW FEATURE
     * It checks if a video has been uploaded for a given ID.
     */

    @PostMapping("/generate")
    public ResponseEntity<String> generateGreetingLink() {
        // Generate simple unique ID - in production could use proper UUID
        String uniqueId = "GREETING_" + System.currentTimeMillis();
        return ResponseEntity.ok(uniqueId);
    }

    @GetMapping(value = "/{uniqueId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateQrCode(@PathVariable String uniqueId) throws Exception {
        // Return placeholder QR - service was migrated
        throw new RuntimeException("QR service temporarily disabled - migration in progress");
    }

    @PostMapping(path = "/{uniqueId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadVideo(
            @PathVariable String uniqueId,
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "message", required = false) String message) {
        // Stub implementation - video upload feature disabled during migration
        return ResponseEntity.ok("Video upload disabled during system upgrade");
    }

    @GetMapping("/{uniqueId}/view")
    public ResponseEntity<?> getGreetingInfo(@PathVariable String uniqueId) {
        // Stub implementation - greeting feature disabled during migration
        return ResponseEntity.status(404).body("Greeting feature temporarily unavailable - system upgrade in progress");
    }
}
