package com.dechub.tanishq.service.storage;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
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
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AWS S3 Storage Implementation
 * Used in preprod and prod environments with IAM role credentials
 */
@Service
@Profile({"preprod", "prod"})
public class AwsS3StorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(AwsS3StorageService.class);

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

            log.info("AWS S3 Storage Service initialized successfully. Bucket: {}, Region: {}", bucketName, region);
        } catch (Exception e) {
            log.error("Failed to initialize S3 client", e);
        }
    }

    @Override
    public String uploadEventFile(MultipartFile file, String eventId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = "event_" + timestamp + "_" + System.currentTimeMillis() + extension;
        String s3Key = "events/" + eventId + "/" + fileName;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            InputStream inputStream = file.getInputStream();
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, s3Key, inputStream, metadata);
            PutObjectResult result = s3Client.putObject(putRequest);

            String s3Url = getS3Url(s3Key);
            log.info("Successfully uploaded file to S3: {} -> {}", fileName, s3Url);
            return s3Url;

        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> uploadEventFiles(List<MultipartFile> files, String eventId) {
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String url = uploadEventFile(file, eventId);
                uploadedUrls.add(url);
            } catch (IOException e) {
                log.error("Failed to upload file: {} for event: {}", file.getOriginalFilename(), eventId, e);
            }
        }
        return uploadedUrls;
    }

    @Override
    public String getEventFolderUrl(String eventId) {
        String folderKey = "events/" + eventId + "/";
        return "s3://" + bucketName + "/" + folderKey;
    }

    @Override
    public String uploadGreetingVideo(MultipartFile file, String greetingId) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = "greeting_video_" + timestamp + "_" + System.currentTimeMillis() + extension;
        String s3Key = "greetings/" + greetingId + "/" + fileName;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            PutObjectRequest putRequest = new PutObjectRequest(
                bucketName,
                s3Key,
                file.getInputStream(),
                metadata
            );
            s3Client.putObject(putRequest);

            String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
            log.info("Successfully uploaded greeting video to S3: {}", s3Url);
            return s3Url;

        } catch (Exception e) {
            log.error("Failed to upload greeting video to S3: {}", fileName, e);
            throw new IOException("Failed to upload to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEventFiles(String eventId) {
        try {
            String folderKey = "events/" + eventId + "/";
            s3Client.listObjects(bucketName, folderKey).getObjectSummaries().forEach(s3Object -> {
                s3Client.deleteObject(bucketName, s3Object.getKey());
                log.info("Deleted S3 object: {}", s3Object.getKey());
            });
        } catch (Exception e) {
            log.error("Failed to delete event files for: {}", eventId, e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            return s3Client != null && s3Client.doesBucketExistV2(bucketName);
        } catch (Exception e) {
            log.error("S3 availability check failed", e);
            return false;
        }
    }

    @Override
    public String generatePresignedUrl(String s3Url, int expirationMinutes) {
        try {
            // Extract S3 key from URL
            String s3Key = extractS3KeyFromUrl(s3Url);

            if (s3Key == null) {
                log.error("Failed to extract S3 key from URL: {}", s3Url);
                return s3Url; // Return original URL as fallback
            }

            // Set expiration time
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += (long) expirationMinutes * 60 * 1000;
            expiration.setTime(expTimeMillis);

            // Generate presigned URL request
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, s3Key)
                    .withMethod(com.amazonaws.HttpMethod.GET)
                    .withExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
            String presignedUrl = url.toString();

            log.info("Generated pre-signed URL for {} (expires in {} minutes)", s3Key, expirationMinutes);
            return presignedUrl;

        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for: {}", s3Url, e);
            return s3Url; // Return original URL as fallback
        }
    }

    /**
     * Extract S3 key from full S3 URL
     * Example: https://bucket-name.s3.region.amazonaws.com/greetings/ID/video.mp4 -> greetings/ID/video.mp4
     */
    private String extractS3KeyFromUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) {
            return null;
        }

        try {
            // Handle format: https://bucket-name.s3.region.amazonaws.com/key/path
            if (s3Url.contains(".amazonaws.com/")) {
                String[] parts = s3Url.split(".amazonaws.com/", 2);
                if (parts.length == 2) {
                    return parts[1];
                }
            }

            // Handle format: s3://bucket-name/key/path
            if (s3Url.startsWith("s3://")) {
                String withoutProtocol = s3Url.substring(5); // Remove "s3://"
                int firstSlash = withoutProtocol.indexOf('/');
                if (firstSlash > 0) {
                    return withoutProtocol.substring(firstSlash + 1);
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting S3 key from URL: {}", s3Url, e);
            return null;
        }
    }

    private String getS3Url(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }
}

