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
            // Generate full URL for QR code so scanners can directly open it
            // Format: https://celebrationsite-preprod.tanishq.co.in/qr?id=GREETING_XXX
            // This allows QR scanner apps to automatically navigate to the upload page
            String qrUrl = greetingBaseUrl.replace("/greetings/", "/qr?id=") + uniqueId;

            log.info("Generating QR code for URL: {}", qrUrl);

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
        // Find greeting
        Optional<Greeting> optGreeting = greetingRepository.findByUniqueId(uniqueId);
        if (!optGreeting.isPresent()) {
            log.error("Greeting not found: {}", uniqueId);
            throw new IllegalArgumentException("Greeting not found: " + uniqueId);
        }

        Greeting greeting = optGreeting.get();

        // Validate video file
        if (videoFile == null || videoFile.isEmpty()) {
            throw new IllegalArgumentException("Video file is required");
        }

        if (videoFile.getSize() > MAX_VIDEO_SIZE) {
            throw new IllegalArgumentException("Video file too large. Maximum size is 100MB");
        }

        // Validate content type
        String contentType = videoFile.getContentType();
        if (contentType == null || !isVideoContentType(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only video files are allowed");
        }

        try {
            // Upload using StorageService (works with both local and AWS S3)
            String videoUrl = storageService.uploadGreetingVideo(videoFile, uniqueId);

            // Update greeting record
            greeting.setGreetingText(name);
            greeting.setMessage(message);
            greeting.setDriveFileId(videoUrl); // Store URL in drive_file_id field
            greeting.setUploaded(true);

            greetingRepository.save(greeting);

            log.info("Successfully uploaded video for greeting: {} -> {}", uniqueId, videoUrl);
            return videoUrl;

        } catch (Exception e) {
            log.error("Failed to upload video for greeting: {}", uniqueId, e);
            throw new IOException("Failed to upload video: " + e.getMessage(), e);
        }
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
}

