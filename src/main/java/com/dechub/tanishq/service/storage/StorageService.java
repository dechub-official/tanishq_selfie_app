package com.dechub.tanishq.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Storage service interface for file upload/download operations
 * Implementations: AWS S3 (preprod/prod) or Local File System (local/dev)
 */
public interface StorageService {

    /**
     * Upload a file to storage under a specific event folder
     * @param file The file to upload
     * @param eventId The event ID (used as folder name)
     * @return The URL/path of the uploaded file
     * @throws IOException If upload fails
     */
    String uploadEventFile(MultipartFile file, String eventId) throws IOException;

    /**
     * Upload multiple files for an event
     * @param files List of files to upload
     * @param eventId The event ID
     * @return List of URLs/paths
     */
    List<String> uploadEventFiles(List<MultipartFile> files, String eventId);

    /**
     * Get the folder URL for an event
     * @param eventId The event ID
     * @return The folder URL/path
     */
    String getEventFolderUrl(String eventId);

    /**
     * Upload a greeting video file
     * @param file Video file
     * @param greetingId Greeting unique ID
     * @return The URL/path of the uploaded file
     * @throws IOException If upload fails
     */
    String uploadGreetingVideo(MultipartFile file, String greetingId) throws IOException;

    /**
     * Delete all files for an event
     * @param eventId The event ID
     */
    void deleteEventFiles(String eventId);

    /**
     * Check if storage service is available
     * @return true if available, false otherwise
     */
    boolean isAvailable();

    /**
     * Generate a pre-signed URL for accessing a greeting video
     * @param s3Url The S3 URL of the video
     * @param expirationMinutes Expiration time in minutes
     * @return Pre-signed URL for temporary access
     */
    String generatePresignedUrl(String s3Url, int expirationMinutes);
}

