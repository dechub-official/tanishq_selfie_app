package com.dechub.tanishq.gdrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "Tanishq";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    // The full 'drive' scope is best for Shared Drives
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    @Value("${dechub.tanishq.google.service.account.event}")
    private String serviceAccountEmail;

    @Value("${dechub.tanishq.key.filepath.event}")
    private String filePath;

    @Value("${dechub.tanishq.google.drive.parent-folder-id.event}")
    private String parentFolderId;

    private Drive getDriveService() throws Exception {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) throws Exception {
        // This now correctly loads the file from src/main/resources
        // The 'filePath' variable is injected by @Value and contains "classpath:your-file.p12"
        ClassPathResource resource = new ClassPathResource(filePath.replace("classpath:", ""));

        InputStream keyFileStream = resource.getInputStream();

        if (keyFileStream == null) {
            throw new Exception("Keys file Not found in classpath: " + filePath);
        }

        // This uses your .p12 file as requested.
        return new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountPrivateKeyFromP12File(keyFileStream) // <-- Use the InputStream
                .setServiceAccountScopes(SCOPES).build();
    }

    /**
     * Uploads a file to a subfolder within the configured parent Shared Drive.
     * @param file The file to upload.
     * @param subFolderName The name of the subfolder to create or use (e.g., eventId).
     * @param mimeType The content type of the file.
     * @return A link to the uploaded file.
     */
    public String uploadFileToDrive(java.io.File file, String subFolderName, String mimeType) throws Exception {
        Drive driveService = getDriveService();
        String subFolderId = findOrCreateFolder(subFolderName, parentFolderId, driveService);

        File fileMetadata = new File();
        fileMetadata.setName(file.getName());
        fileMetadata.setParents(Collections.singletonList(subFolderId));

        FileContent mediaContent = new FileContent(mimeType, file);

        driveService.files().create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id") // we don’t need webViewLink now
                .execute();

        // ✅ Instead of returning the file link, return the folder link
        return getFolderLink(subFolderId);
    }


    /**
     * Finds a subfolder by name within a parent folder/drive, or creates it if it doesn't exist.
     */
    private String findOrCreateFolder(String folderName, String parentId, Drive service) throws Exception {
        String query = "name = '" + folderName + "' and mimeType = 'application/vnd.google-apps.folder' and '" + parentId + "' in parents and trashed = false";

        FileList result = service.files().list()
                .setQ(query)
                .setSupportsAllDrives(true)             // <-- IMPORTANT
                .setIncludeItemsFromAllDrives(true)     // <-- IMPORTANT
                .setSpaces("drive")
                .setFields("files(id)")
                .execute();

        if (result.getFiles().isEmpty()) {
            // Folder doesn't exist, create it
            File folderMetadata = new File();
            folderMetadata.setName(folderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setParents(Collections.singletonList(parentId));

            File folder = service.files().create(folderMetadata)
                    .setSupportsAllDrives(true) // <-- IMPORTANT
                    .setFields("id")
                    .execute();
            return folder.getId();
        } else {
            // Folder exists, return its ID
            return result.getFiles().get(0).getId();
        }
    }
    public String getFolderLink(String folderId) {
        return "https://drive.google.com/drive/folders/" + folderId;
    }
    public String getFolderLinkForEvent(String eventId) throws Exception {
        Drive driveService = getDriveService();
        String subFolderId = findOrCreateFolder(eventId, parentFolderId, driveService);
        return getFolderLink(subFolderId);
    }


}