package com.dechub.tanishq.service;

import com.dechub.tanishq.entity.Greeting;
import com.dechub.tanishq.repository.GreetingRepository;
import com.dechub.tanishq.service.storage.StorageService;
import com.dechub.tanishq.service.qr.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing video greeting cards
 * Uses MySQL for greeting metadata and StorageService for video storage
 */
@Slf4j
@Service
public class GreetingService {

    @Autowired
    private GreetingRepository greetingRepository;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private StorageService storageService;

    @Value("${greeting.qr.base.url:https://celebrationsite-preprod.tanishq.co.in/greetings/}")
    private String greetingBaseUrl;


    // Maximum video file size (100 MB)
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

    /**
     * Create a new greeting with unique ID
     * @return Unique greeting ID
     */
    public String createGreeting() {
        try {
            String uniqueId = "GREETING_" + System.currentTimeMillis();

            Greeting greeting = new Greeting();
            greeting.setUniqueId(uniqueId);
            greeting.setUploaded(false);
            greeting.setCreatedAt(LocalDateTime.now());

            greetingRepository.save(greeting);

            log.info("Created new greeting with ID: {}", uniqueId);
            return uniqueId;
        } catch (Exception e) {
            log.error("Failed to create greeting", e);
            throw new RuntimeException("Failed to create greeting: " + e.getMessage());
        }
    }

    /**
     * Generate QR code for a greeting
     * @param uniqueId Greeting unique ID
     * @return QR code image as byte array (PNG)
     */
    public byte[] generateQrCode(String uniqueId) throws Exception {
        Optional<Greeting> optGreeting = greetingRepository.findByUniqueId(uniqueId);

        if (!optGreeting.isPresent()) {
            log.error("Greeting not found: {}", uniqueId);
            throw new IllegalArgumentException("Greeting not found: " + uniqueId);
        }

        Greeting greeting = optGreeting.get();

        try {
            // Generate full URL for QR code that points to the React app
            // Direct link to create-video page with qrId parameter (NOT id!)
            // The frontend looks for 'qrId' parameter, NOT 'id'
            // When scanned directly with phone camera, this ensures the qrId is captured
            // Format: https://celebrations.tanishq.co.in/qr/create-video?qrId=GREETING_XXX&autoStart=true
            String qrUrl = "https://celebrations.tanishq.co.in/qr/create-video?qrId=" + uniqueId + "&autoStart=true";

            log.info("Generating QR code for greeting URL: {}", qrUrl);

            // Generate QR code with full URL
            byte[] qrCodeImage = qrCodeService.generateQrCodeImage(qrUrl, 300, 300);

            // Save QR code data to database (Base64) for future reference
            // Wrap in try-catch to handle potential database save issues
            try {
                String qrCodeBase64 = java.util.Base64.getEncoder().encodeToString(qrCodeImage);
                greeting.setQrCodeData(qrCodeBase64);
                greetingRepository.save(greeting);
                log.debug("✓ QR code data saved to database for greeting: {}", uniqueId);
            } catch (Exception dbEx) {
                log.warn("⚠ Failed to save QR code data to database for greeting: {} - {}", uniqueId, dbEx.getMessage());
                log.warn("   QR code is still generated and returned, just not cached in database");
                // Continue anyway - QR code is generated, just not saved to DB
            }

            log.info("✓ Generated QR code successfully for greeting: {}", uniqueId);
            return qrCodeImage;

        } catch (Exception ex) {
            log.error("✗ Failed to generate QR code image for greeting: {}", uniqueId, ex);
            throw new Exception("QR code generation failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Upload video for a greeting to S3
     * @param uniqueId Greeting unique ID
     * @param videoFile Video file
     * @param name Sender name
     * @param message Personal message
     * @return S3 URL of uploaded video
     */
    public String uploadVideo(String uniqueId, MultipartFile videoFile, String name, String message) throws IOException {
        log.info("uploadVideo called - uniqueId: {}, name: {}, message: {}, videoFile size: {}",
                 uniqueId, name, message, videoFile != null ? videoFile.getSize() : 0);

        // Find greeting
        Optional<Greeting> optGreeting = greetingRepository.findByUniqueId(uniqueId);
        if (!optGreeting.isPresent()) {
            log.error("Greeting not found: {}", uniqueId);
            throw new IllegalArgumentException("Greeting not found: " + uniqueId);
        }

        Greeting greeting = optGreeting.get();

        // Validate video file
        if (videoFile == null || videoFile.isEmpty()) {
            log.error("Video file is null or empty");
            throw new IllegalArgumentException("Video file is required");
        }

        if (videoFile.getSize() > MAX_VIDEO_SIZE) {
            log.error("Video file size {} exceeds limit {}", videoFile.getSize(), MAX_VIDEO_SIZE);
            throw new IllegalArgumentException("Video file too large. Maximum size is 100MB");
        }

        // Validate content type
        String contentType = videoFile.getContentType();
        if (contentType == null || !isVideoContentType(contentType)) {
            log.warn("Invalid content type: {}", contentType);
            throw new IllegalArgumentException("Invalid file type. Only video files are allowed");
        }

        // Sanitize and validate text fields
        String sanitizedName = sanitizeText(name, "Name");
        String sanitizedMessage = sanitizeText(message, "Message");

        log.info("Sanitized inputs - name: {}, message: {}", sanitizedName, sanitizedMessage);

        try {
            // Upload using StorageService (works with both local and AWS S3)
            String videoUrl = storageService.uploadGreetingVideo(videoFile, uniqueId);
            log.info("Video uploaded successfully to: {}", videoUrl);

            // Update greeting record
            greeting.setGreetingText(sanitizedName);
            greeting.setMessage(sanitizedMessage);
            greeting.setDriveFileId(videoUrl); // Store URL in drive_file_id field
            greeting.setUploaded(true);

            greetingRepository.save(greeting);

            log.info("Successfully uploaded video and saved greeting: {} -> {}", uniqueId, videoUrl);
            return videoUrl;

        } catch (Exception e) {
            log.error("Failed to upload video for greeting: {}", uniqueId, e);
            throw new IOException("Failed to upload video: " + e.getMessage(), e);
        }
    }

    /**
     * Sanitize text input to handle encoding issues from mobile browsers
     * @param text Input text
     * @param fieldName Field name for logging
     * @return Sanitized text
     */
    private String sanitizeText(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("{} is null or empty", fieldName);
            return "";
        }

        // Trim whitespace
        text = text.trim();

        // Convert to proper UTF-8 encoding if needed
        try {
            // Detect and fix encoding issues
            byte[] bytes = text.getBytes("ISO-8859-1");
            text = new String(bytes, "UTF-8");
        } catch (Exception e) {
            log.debug("Text encoding conversion not needed for {}", fieldName);
        }

        // Remove any control characters except newlines and tabs
        text = text.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "");

        // Normalize whitespace (convert multiple spaces to single space)
        text = text.replaceAll("\\s+", " ");

        // Trim again after sanitization
        text = text.trim();

        // Validate length
        if (fieldName.equals("Name")) {
            if (text.length() < 2) {
                throw new IllegalArgumentException("Name must be at least 2 characters");
            }
            if (text.length() > 100) {
                text = text.substring(0, 100);
                log.warn("Name truncated to 100 characters");
            }
        } else if (fieldName.equals("Message")) {
            if (text.length() < 10) {
                throw new IllegalArgumentException("Message must be at least 10 characters");
            }
            if (text.length() > 500) {
                text = text.substring(0, 500);
                log.warn("Message truncated to 500 characters");
            }
        }

        log.debug("Sanitized {}: {}", fieldName, text);
        return text;
    }

    /**
     * Get greeting by unique ID
     * @param uniqueId Greeting unique ID
     * @return Optional of Greeting
     */
    public Optional<Greeting> getGreeting(String uniqueId) {
        return greetingRepository.findByUniqueId(uniqueId);
    }

    /**
     * Get video playback URL
     * @param s3Url S3 URL stored in database
     * @return Playback URL
     */
    public String getVideoPlaybackUrl(String s3Url) {
        // If it's already a full URL, return as is
        if (s3Url != null && (s3Url.startsWith("https://") || s3Url.startsWith("http://"))) {
            return s3Url;
        }
        // Otherwise construct the URL
        return s3Url;
    }


    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * Check if content type is video
     */
    private boolean isVideoContentType(String contentType) {
        return contentType.startsWith("video/") ||
               contentType.equals("application/octet-stream"); // Some mobile devices use this
    }

    /**
     * Delete greeting and associated video from storage
     * @param uniqueId Greeting unique ID
     */
    public void deleteGreeting(String uniqueId) {
        try {
            Optional<Greeting> optGreeting = greetingRepository.findByUniqueId(uniqueId);
            if (optGreeting.isPresent()) {
                Greeting greeting = optGreeting.get();

                // Delete greeting record
                greetingRepository.delete(greeting);

                log.info("Deleted greeting: {}", uniqueId);
            }
        } catch (Exception e) {
            log.error("Failed to delete greeting: {}", uniqueId, e);
        }
    }


    /**
     * Check if greeting exists and has video uploaded
     * @param uniqueId Greeting unique ID
     * @return true if video uploaded, false otherwise
     */
    public boolean hasVideoUploaded(String uniqueId) {
        Optional<Greeting> optGreeting = greetingRepository.findByUniqueId(uniqueId);
        return optGreeting.isPresent() && optGreeting.get().getUploaded();
    }

    /**
     * Generate a fresh pre-signed URL for video playback
     * This solves the 403 Forbidden issue when users rescan QR codes
     *
     * @param s3Url The S3 URL stored in database
     * @return Fresh pre-signed URL valid for 10 minutes
     */
    public String generateFreshVideoUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) {
            log.warn("Attempted to generate pre-signed URL for null/empty S3 URL");
            return null;
        }

        try {
            // Generate pre-signed URL with 10-minute expiration
            // This allows users to:
            // - Watch the full video
            // - Replay it within the session
            // But prevents long-term URL sharing
            String presignedUrl = storageService.generatePresignedUrl(s3Url, 10);
            log.info("Generated fresh pre-signed URL (expires in 10 minutes)");
            return presignedUrl;
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for: {}", s3Url, e);
            // Fallback to original URL (will fail for private buckets but prevents crashes)
            return s3Url;
        }
    }
}

