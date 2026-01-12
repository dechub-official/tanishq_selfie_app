package com.dechub.tanishq.controller;

import com.dechub.tanishq.dto.qrcode.GreetingInfo;
import com.dechub.tanishq.entity.Greeting;
import com.dechub.tanishq.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Controller for managing video greeting cards
 * Endpoints for creating, uploading, and viewing greetings
 */
@Slf4j
@RestController
@RequestMapping("/greetings")
public class GreetingController {

    @Autowired
    private GreetingService greetingService;

    /**
     * Generate a new greeting link with unique ID
     * POST /greetings/generate
     *
     * @return Unique greeting ID
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateGreetingLink() {
        try {
            String uniqueId = greetingService.createGreeting();
            log.info("Generated greeting link: {}", uniqueId);
            return ResponseEntity.ok(uniqueId);
        } catch (Exception e) {
            log.error("Failed to generate greeting link", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating greeting: " + e.getMessage());
        }
    }

    /**
     * Generate QR code for a greeting
     * GET /greetings/{uniqueId}/qr
     *
     * @param uniqueId Greeting unique ID
     * @return QR code image (PNG)
     */
    @GetMapping(value = "/{uniqueId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(@PathVariable String uniqueId) {
        log.info("Received QR code generation request for greeting: {}", uniqueId);

        try {
            byte[] qrCode = greetingService.generateQrCode(uniqueId);
            log.info("Successfully generated QR code for greeting: {} (size: {} bytes)", uniqueId, qrCode.length);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCode);
        } catch (IllegalArgumentException e) {
            log.error("Greeting not found: {}", uniqueId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to generate QR code for greeting: {} - Error: {}", uniqueId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload video for a greeting
     * POST /greetings/{uniqueId}/upload
     *
     * @param uniqueId Greeting unique ID
     * @param videoFile Video file (multipart)
     * @param name Sender name (optional)
     * @param message Personal message (optional)
     * @return Success message with S3 URL
     */
    @PostMapping(path = "/{uniqueId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadVideo(
            @PathVariable String uniqueId,
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "message", required = false) String message) {
        try {
            String s3Url = greetingService.uploadVideo(uniqueId, videoFile, name, message);
            log.info("Uploaded video for greeting: {} -> {}", uniqueId, s3Url);
            return ResponseEntity.ok("Video uploaded successfully. URL: " + s3Url);
        } catch (IllegalArgumentException e) {
            log.error("Invalid upload request for greeting: {}", uniqueId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to upload video for greeting: {}", uniqueId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Get greeting information and check if video uploaded
     * GET /greetings/{uniqueId}/view
     *
     * @param uniqueId Greeting unique ID
     * @return Greeting info with video URL if uploaded
     */
    @GetMapping("/{uniqueId}/view")
    public ResponseEntity<?> getGreetingInfo(@PathVariable String uniqueId) {
        try {
            Optional<Greeting> optGreeting = greetingService.getGreeting(uniqueId);

            if (!optGreeting.isPresent()) {
                log.error("Greeting not found: {}", uniqueId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Greeting not found");
            }

            Greeting greeting = optGreeting.get();

            // Check if video uploaded
            if (!greeting.getUploaded()) {
                log.debug("No video uploaded yet for greeting: {}", uniqueId);
                GreetingInfo info = new GreetingInfo(
                        false,
                        "pending",
                        null,
                        null,
                        null,
                        null,
                        null
                );
                return ResponseEntity.ok(info);
            }

            // Video uploaded - return full info
            String videoUrl = greetingService.getVideoPlaybackUrl(greeting.getDriveFileId());
            String timestamp = greeting.getCreatedAt() != null ?
                    greeting.getCreatedAt().toString() : null;

            GreetingInfo info = new GreetingInfo(
                    true,
                    "completed",
                    greeting.getDriveFileId(),
                    videoUrl,
                    timestamp,
                    greeting.getGreetingText(),
                    greeting.getMessage()
            );

            log.info("Retrieved greeting info: {}", uniqueId);
            return ResponseEntity.ok(info);

        } catch (Exception e) {
            log.error("Failed to get greeting info: {}", uniqueId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete a greeting (admin only - add security as needed)
     * DELETE /greetings/{uniqueId}
     *
     * @param uniqueId Greeting unique ID
     * @return Success message
     */
    @DeleteMapping("/{uniqueId}")
    public ResponseEntity<String> deleteGreeting(@PathVariable String uniqueId) {
        try {
            greetingService.deleteGreeting(uniqueId);
            log.info("Deleted greeting: {}", uniqueId);
            return ResponseEntity.ok("Greeting deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete greeting: {}", uniqueId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete greeting: " + e.getMessage());
        }
    }

    /**
     * Check if greeting has video uploaded
     * GET /greetings/{uniqueId}/status
     *
     * @param uniqueId Greeting unique ID
     * @return JSON with upload status
     */
    @GetMapping("/{uniqueId}/status")
    public ResponseEntity<?> checkUploadStatus(@PathVariable String uniqueId) {
        try {
            boolean hasVideo = greetingService.hasVideoUploaded(uniqueId);
            return ResponseEntity.ok(new StatusResponse(hasVideo));
        } catch (Exception e) {
            log.error("Failed to check upload status: {}", uniqueId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking status");
        }
    }

    /**
     * Simple status response DTO
     */
    private static class StatusResponse {
        public boolean uploaded;

        public StatusResponse(boolean uploaded) {
            this.uploaded = uploaded;
        }
    }
}
