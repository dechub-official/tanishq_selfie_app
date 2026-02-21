package com.dechub.tanishq.service.aws;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AWS S3 Service for uploading event images/videos
 * Replaces Google Drive integration
 * NOTE: This class is DEPRECATED - use StorageService interface instead
 * Kept for backward compatibility in preprod/prod
 */
@Service
@Profile({"preprod", "prod"})
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        try {
            // Use IAM role credentials from EC2 instance
            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(new InstanceProfileCredentialsProvider(false))
                    .build();

            log.info("S3 Service initialized successfully. Bucket: {}, Region: {}", bucketName, region);
        } catch (Exception e) {
            log.error("Failed to initialize S3 client", e);
        }
    }

    /**
     * Upload a file to S3 under a specific event folder
     *
     * @param file The file to upload
     * @param eventId The event ID (used as folder name)
     * @return The S3 URL of the uploaded file
     * @throws IOException If upload fails
     */
    public String uploadEventFile(MultipartFile file, String eventId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Generate unique filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = "event_" + timestamp + "_" + System.currentTimeMillis() + extension;

        // Create S3 key (folder structure): events/{eventId}/{filename}
        String s3Key = "events/" + eventId + "/" + fileName;

        try {
            // Prepare metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // Upload to S3
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, s3Key, inputStream, metadata);
            PutObjectResult result = s3Client.putObject(putRequest);

            // Generate S3 URL
            String s3Url = getS3Url(s3Key);

            log.info("Successfully uploaded file to S3: {} -> {}", fileName, s3Url);
            return s3Url;

        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload multiple files for an event
     *
     * @param files List of files to upload
     * @param eventId The event ID
     * @return List of S3 URLs
     */
    public List<String> uploadEventFiles(List<MultipartFile> files, String eventId) {
        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String url = uploadEventFile(file, eventId);
                uploadedUrls.add(url);
            } catch (IOException e) {
                log.error("Failed to upload file: {} for event: {}",
                         file.getOriginalFilename(), eventId, e);
                // Continue with other files
            }
        }

        return uploadedUrls;
    }

    /**
     * Get the S3 folder URL for an event
     *
     * @param eventId The event ID
     * @return The S3 folder URL (for saving in database)
     */
    public String getEventFolderUrl(String eventId) {
        String folderKey = "events/" + eventId + "/";
        return "s3://" + bucketName + "/" + folderKey;
    }


    /**
     * Get the HTTPS URL for accessing a file
     *
     * @param s3Key The S3 key of the file
     * @return The HTTPS URL
     */
    private String getS3Url(String s3Key) {
        // Format: https://{bucket}.s3.{region}.amazonaws.com/{key}
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                           bucketName, region, s3Key);
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
     * Delete all files for an event (if needed)
     *
     * @param eventId The event ID
     */
    public void deleteEventFiles(String eventId) {
        try {
            String folderKey = "events/" + eventId + "/";
            // List and delete all objects in the folder
            s3Client.listObjects(bucketName, folderKey).getObjectSummaries().forEach(s3Object -> {
                s3Client.deleteObject(bucketName, s3Object.getKey());
                log.info("Deleted S3 object: {}", s3Object.getKey());
            });
        } catch (Exception e) {
            log.error("Failed to delete event files for: {}", eventId, e);
        }
    }

    /**
     * Check if S3 client is initialized and working
     */
    public boolean isAvailable() {
        try {
            return s3Client != null && s3Client.doesBucketExistV2(bucketName);
        } catch (Exception e) {
            log.error("S3 availability check failed", e);
            return false;
        }
    }
}

