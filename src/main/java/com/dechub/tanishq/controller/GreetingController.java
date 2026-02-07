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

        log.info("=== GREETING UPLOAD REQUEST ===");
        log.info("UniqueId: {}", uniqueId);
        log.info("Name: {}", name);
        log.info("Message: {}", message);
        log.info("Video File: {}", videoFile != null ? videoFile.getOriginalFilename() : "NULL");
        log.info("Video Size: {} bytes", videoFile != null ? videoFile.getSize() : 0);
        log.info("Content Type: {}", videoFile != null ? videoFile.getContentType() : "NULL");
        log.info("==============================");

        try {
            // Validate uniqueId is not null or "null" string
            if (uniqueId == null || uniqueId.trim().isEmpty() || "null".equalsIgnoreCase(uniqueId.trim()) || "undefined".equalsIgnoreCase(uniqueId.trim())) {
                log.error("Invalid uniqueId received: '{}' - This indicates a frontend issue where qrId is not being set properly", uniqueId);
                return ResponseEntity.badRequest()
                    .body("Invalid greeting ID. Please scan the QR code again or refresh the page.");
            }

            // Validate video file
            if (videoFile == null || videoFile.isEmpty()) {
                log.error("Video file is null or empty for greeting: {}", uniqueId);
                return ResponseEntity.badRequest().body("Video file is required. Please record a video before submitting.");
            }

            // Validate file size (max 100MB)
            long maxSize = 100 * 1024 * 1024; // 100MB
            if (videoFile.getSize() > maxSize) {
                log.error("Video file too large: {} bytes for greeting: {}", videoFile.getSize(), uniqueId);
                return ResponseEntity.badRequest()
                    .body("Video file is too large. Please record a shorter video (max 100MB).");
            }

            // Validate video file has actual content
            if (videoFile.getSize() < 1024) { // Less than 1KB is suspicious
                log.error("Video file suspiciously small: {} bytes for greeting: {}", videoFile.getSize(), uniqueId);
                return ResponseEntity.badRequest()
                    .body("Video file appears to be invalid. Please try recording again.");
            }

            // Validate content type (allow common video formats)
            String contentType = videoFile.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                log.warn("Non-video content type received: {} for greeting: {}", contentType, uniqueId);
                // Don't fail, just log warning - mobile might send different content types
            }

            // Validate name
            if (name == null || name.trim().isEmpty()) {
                log.error("Name is empty for greeting: {}", uniqueId);
                return ResponseEntity.badRequest().body("Please enter your name.");
            }

            if (name.trim().length() < 2) {
                log.error("Name too short for greeting: {}", uniqueId);
                return ResponseEntity.badRequest().body("Name must be at least 2 characters long.");
            }

            // Validate message
            if (message == null || message.trim().isEmpty()) {
                log.error("Message is empty for greeting: {}", uniqueId);
                return ResponseEntity.badRequest().body("Please enter your message.");
            }

            if (message.trim().length() < 10) {
                log.error("Message too short for greeting: {}", uniqueId);
                return ResponseEntity.badRequest().body("Message must be at least 10 characters long.");
            }

            String s3Url = greetingService.uploadVideo(uniqueId, videoFile, name, message);
            log.info("✓ Successfully uploaded video for greeting: {} -> {}", uniqueId, s3Url);
            return ResponseEntity.ok("Video uploaded successfully!");

        } catch (IllegalArgumentException e) {
            log.error("✗ Validation error for greeting: {} - {}", uniqueId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("✗ Failed to upload video for greeting: {} - {}", uniqueId, e.getMessage(), e);

            // Provide user-friendly error message
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("encoding")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was a problem with the text encoding. Please check your message and try again.");
            } else if (errorMsg != null && errorMsg.contains("storage")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save video. Please check your connection and try again.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed. Please try again. If the problem persists, try recording a shorter video.");
            }
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
        log.info("Received view request for greeting: {}", uniqueId);

        try {
            // Validate uniqueId is not null or "null" string
            if (uniqueId == null || uniqueId.trim().isEmpty() || "null".equalsIgnoreCase(uniqueId.trim()) || "undefined".equalsIgnoreCase(uniqueId.trim())) {
                log.error("Invalid uniqueId received for view: '{}' - This indicates a frontend issue where qrId is not being set properly", uniqueId);
                return ResponseEntity.badRequest()
                    .body("Invalid greeting ID. Please scan the QR code again or refresh the page.");
            }

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
            String s3Url = greeting.getDriveFileId(); // This field stores S3 URL now
            String videoUrl = greetingService.getVideoPlaybackUrl(s3Url);
            String timestamp = greeting.getCreatedAt() != null ?
                    greeting.getCreatedAt().toString() : null;

            GreetingInfo info = new GreetingInfo(
                    true,
                    "completed",
                    null,  // driveFileId should be null (we use S3 now)
                    videoUrl,  // videoPlaybackUrl contains the S3 URL
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
     * Get fresh pre-signed video URL for playback
     * GET /greetings/{uniqueId}/video-url
     *
     * This endpoint generates a fresh pre-signed S3 URL every time it's called,
     * solving the 403 Forbidden issue when users rescan QR codes
     *
     * @param uniqueId Greeting unique ID
     * @return JSON with video playback URL
     */
    @GetMapping("/{uniqueId}/video-url")
    public ResponseEntity<?> getVideoPlaybackUrl(@PathVariable String uniqueId) {
        try {
            Optional<Greeting> optGreeting = greetingService.getGreeting(uniqueId);

            if (!optGreeting.isPresent()) {
                log.error("Greeting not found: {}", uniqueId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Greeting not found"));
            }

            Greeting greeting = optGreeting.get();

            // Check if video uploaded
            if (!greeting.getUploaded() || greeting.getDriveFileId() == null) {
                log.debug("No video uploaded yet for greeting: {}", uniqueId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Video not uploaded yet"));
            }

            // Generate fresh pre-signed URL (expires in 10 minutes)
            String s3Url = greeting.getDriveFileId();
            String presignedUrl = greetingService.generateFreshVideoUrl(s3Url);

            log.info("Generated fresh video URL for greeting: {}", uniqueId);
            return ResponseEntity.ok(new VideoUrlResponse(presignedUrl));

        } catch (Exception e) {
            log.error("Failed to get video URL for greeting: {}", uniqueId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error: " + e.getMessage()));
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

    /**
     * Video URL response DTO
     */
    private static class VideoUrlResponse {
        public String videoUrl;

        public VideoUrlResponse(String videoUrl) {
            this.videoUrl = videoUrl;
        }
    }

    /**
     * Error response DTO
     */
    private static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
