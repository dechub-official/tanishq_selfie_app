package com.dechub.tanishq.service.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Local File System Storage Implementation
 * Used in local development environment (no AWS dependencies)
 */
@Service
@Profile("local")
public class LocalFileStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    @Value("${local.storage.base.path:./storage}")
    private String basePath;

    @Value("${local.storage.base.url:http://localhost:3000/storage}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            // Create base storage directory if it doesn't exist
            Path baseDir = Paths.get(basePath);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
                log.info("Created local storage directory: {}", baseDir.toAbsolutePath());
            }
            log.info("Local File Storage Service initialized. Base path: {}", baseDir.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to initialize local storage directory", e);
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

        // Create folder structure: {basePath}/events/{eventId}/
        Path eventDir = Paths.get(basePath, "events", eventId);
        Files.createDirectories(eventDir);

        // Save file
        Path filePath = eventDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Generate local URL
        String fileUrl = baseUrl + "/events/" + eventId + "/" + fileName;
        log.info("Successfully saved file locally: {} -> {}", fileName, filePath.toAbsolutePath());
        return fileUrl;
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
        return baseUrl + "/events/" + eventId + "/";
    }

    @Override
    public String uploadGreetingVideo(MultipartFile file, String greetingId) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = "greeting_video_" + timestamp + "_" + System.currentTimeMillis() + extension;

        // Create folder structure: {basePath}/greetings/{greetingId}/
        Path greetingDir = Paths.get(basePath, "greetings", greetingId);
        Files.createDirectories(greetingDir);

        // Save file
        Path filePath = greetingDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Generate local URL
        String fileUrl = baseUrl + "/greetings/" + greetingId + "/" + fileName;
        log.info("Successfully saved greeting video locally: {} -> {}", fileName, filePath.toAbsolutePath());
        return fileUrl;
    }

    @Override
    public String uploadGreetingVideoFromFile(java.io.File file, String greetingId) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getName();
        String extension = getFileExtension(originalFilename);
        String fileName = "greeting_video_" + timestamp + "_" + System.currentTimeMillis() + extension;

        // Create folder structure: {basePath}/greetings/{greetingId}/
        Path greetingDir = Paths.get(basePath, "greetings", greetingId);
        Files.createDirectories(greetingDir);

        // Save file
        Path filePath = greetingDir.resolve(fileName);
        Files.copy(file.toPath(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Generate local URL
        String fileUrl = baseUrl + "/greetings/" + greetingId + "/" + fileName;
        log.info("Successfully saved greeting video from file locally: {} -> {}", fileName, filePath.toAbsolutePath());
        return fileUrl;
    }

    @Override
    public void deleteEventFiles(String eventId) {
        try {
            Path eventDir = Paths.get(basePath, "events", eventId);
            if (Files.exists(eventDir)) {
                Files.walk(eventDir)
                    .sorted((a, b) -> b.compareTo(a)) // Reverse order for deletion
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted local file: {}", path);
                        } catch (IOException e) {
                            log.error("Failed to delete: {}", path, e);
                        }
                    });
            }
        } catch (Exception e) {
            log.error("Failed to delete event files for: {}", eventId, e);
        }
    }

    @Override
    public boolean isAvailable() {
        Path baseDir = Paths.get(basePath);
        return Files.exists(baseDir) && Files.isDirectory(baseDir);
    }

    @Override
    public String generatePresignedUrl(String s3Url, int expirationMinutes) {
        // Local files are publicly accessible, so just return the URL as-is
        // No pre-signing needed for local development
        log.debug("Local storage does not require pre-signed URLs, returning original URL");
        return s3Url;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }
}

