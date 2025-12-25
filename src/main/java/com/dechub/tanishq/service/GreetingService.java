package com.dechub.tanishq.service;

import com.dechub.tanishq.entity.Greeting;
import com.dechub.tanishq.repository.GreetingRepository;
import com.dechub.tanishq.service.aws.S3Service;
import com.dechub.tanishq.service.qr.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Service for managing video greeting cards
 * Uses MySQL for greeting metadata and AWS S3 for video storage
 */
@Slf4j
@Service
public class GreetingService {

    @Autowired
    private GreetingRepository greetingRepository;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private S3Service s3Service;

    @Value("${greeting.qr.base.url:https://celebrationsite-preprod.tanishq.co.in/greetings/}")
    private String greetingBaseUrl;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

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

        // Encode ONLY the uniqueId (old behavior - frontend decides navigation)
        log.debug("Generating QR code for uniqueId: {}", uniqueId);

        // Generate QR code with just the uniqueId
        byte[] qrCodeImage = qrCodeService.generateQrCodeImage(uniqueId, 300, 300);

        // Save QR code data to database (Base64) for future reference
        String qrCodeBase64 = java.util.Base64.getEncoder().encodeToString(qrCodeImage);
        greeting.setQrCodeData(qrCodeBase64);
        greetingRepository.save(greeting);

        log.info("Generated QR code for greeting: {}", uniqueId);
        return qrCodeImage;
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
            // Upload to S3 under greetings folder
            String s3Url = uploadGreetingVideoToS3(videoFile, uniqueId);

            // Update greeting record
            greeting.setGreetingText(name);
            greeting.setMessage(message);
            greeting.setDriveFileId(s3Url); // Store S3 URL in drive_file_id field
            greeting.setUploaded(true);

            greetingRepository.save(greeting);

            log.info("Successfully uploaded video for greeting: {} -> {}", uniqueId, s3Url);
            return s3Url;

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
        // If it's already a full S3 URL, return as is
        if (s3Url != null && (s3Url.startsWith("https://") || s3Url.startsWith("http://"))) {
            return s3Url;
        }
        // Otherwise construct the URL
        return s3Url;
    }

    /**
     * Upload greeting video to S3
     * @param file Video file
     * @param greetingId Greeting unique ID
     * @return S3 URL
     */
    private String uploadGreetingVideoToS3(MultipartFile file, String greetingId) throws IOException {
        // Generate unique filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = "greeting_video_" + timestamp + "_" + System.currentTimeMillis() + extension;

        // Create S3 key: greetings/{greetingId}/{filename}
        String s3Key = "greetings/" + greetingId + "/" + fileName;

        try {
            // Upload to S3 using the existing S3Service
            // Since S3Service.uploadEventFile uses "events/" prefix, we'll implement direct upload here
            com.amazonaws.services.s3.AmazonS3 s3Client = getS3Client();

            com.amazonaws.services.s3.model.ObjectMetadata metadata = new com.amazonaws.services.s3.model.ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            com.amazonaws.services.s3.model.PutObjectRequest putRequest =
                new com.amazonaws.services.s3.model.PutObjectRequest(
                    bucketName,
                    s3Key,
                    file.getInputStream(),
                    metadata
                );

            s3Client.putObject(putRequest);

            // Generate S3 URL
            String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, s3Key);

            log.info("Successfully uploaded greeting video to S3: {}", s3Url);
            return s3Url;

        } catch (Exception e) {
            log.error("Failed to upload greeting video to S3: {}", fileName, e);
            throw new IOException("Failed to upload to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Get S3 client instance
     */
    private com.amazonaws.services.s3.AmazonS3 getS3Client() {
        return com.amazonaws.services.s3.AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new com.amazonaws.auth.InstanceProfileCredentialsProvider(false))
                .build();
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
     * Delete greeting and associated video from S3
     * @param uniqueId Greeting unique ID
     */
    public void deleteGreeting(String uniqueId) {
        try {
            Optional<Greeting> optGreeting = greetingRepository.findByUniqueId(uniqueId);
            if (optGreeting.isPresent()) {
                Greeting greeting = optGreeting.get();

                // Delete video from S3 if exists
                if (greeting.getDriveFileId() != null && !greeting.getDriveFileId().isEmpty()) {
                    deleteVideoFromS3(greeting.getDriveFileId());
                }

                // Delete greeting record
                greetingRepository.delete(greeting);

                log.info("Deleted greeting: {}", uniqueId);
            }
        } catch (Exception e) {
            log.error("Failed to delete greeting: {}", uniqueId, e);
        }
    }

    /**
     * Delete video from S3
     */
    private void deleteVideoFromS3(String s3Url) {
        try {
            // Extract S3 key from URL
            // Format: https://{bucket}.s3.{region}.amazonaws.com/{key}
            if (s3Url != null && s3Url.contains(".amazonaws.com/")) {
                String s3Key = s3Url.substring(s3Url.indexOf(".amazonaws.com/") + 15);

                com.amazonaws.services.s3.AmazonS3 s3Client = getS3Client();
                s3Client.deleteObject(bucketName, s3Key);

                log.info("Deleted video from S3: {}", s3Key);
            }
        } catch (Exception e) {
            log.error("Failed to delete video from S3: {}", s3Url, e);
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

