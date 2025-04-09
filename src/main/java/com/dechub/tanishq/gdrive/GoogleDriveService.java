package com.dechub.tanishq.gdrive;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.http.HttpCredentialsAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "Tanishq";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/drive.file", "https://www.googleapis.com/auth/spreadsheets");


    @Value("${dechub.tanishq.google.service.account}")
    private String serviceAccountEmail;

    @Value("${dechub.tanishq.key.filepath}")
    private String serviceAccountFilePath;

    @Value("${dechub.tanishq.key.filepath}")
    private String filePath;

    public void uploadVideo(MultipartFile videoFile) throws Exception {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = getDriveService(HTTP_TRANSPORT);

        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(videoFile.getOriginalFilename()); // Name of the video file
        fileMetadata.setMimeType(videoFile.getContentType());

        ByteArrayContent mediaContent = new ByteArrayContent(videoFile.getContentType(), videoFile.getBytes());

        File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        System.out.println("File ID: " + uploadedFile.getId());
    }

    private Drive getDriveService(NetHttpTransport HTTP_TRANSPORT) throws Exception {
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    private Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) throws Exception {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            throw new Exception("Keys file Not found");
        }

        Credential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountPrivateKeyFromP12File(file)
                .setServiceAccountScopes(SCOPES).build();
        return credential;
    }

    public String uploadFileToDrive(java.io.File file, String id, String mimeType) throws Exception {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive driveService = getDriveService(HTTP_TRANSPORT);

        // Check if the folder with the given name exists
        String folderId = null;
        String query = "name = '" + id + "' and mimeType = 'application/vnd.google-apps.folder' and '1bcjNNlxbOn_AA9rbff6JXsDUpAuUGxh1' in parents";
        FileList result = driveService.files().list().setQ(query).setSpaces("drive").setFields("files(id, name)").execute();

        if (result.getFiles().isEmpty()) {
            // Create a folder with the name as the provided id
            File folderMetadata = new File();
            folderMetadata.setName(id);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setParents(Collections.singletonList("1bcjNNlxbOn_AA9rbff6JXsDUpAuUGxh1"));

            File folder = driveService.files().create(folderMetadata)
                    .setFields("id")
                    .execute();
            folderId = folder.getId();
        } else {
            folderId = result.getFiles().get(0).getId();
        }

        // Upload the file inside the identified or created folder
        File fileMetadata = new File();
        fileMetadata.setName(file.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent = new FileContent(mimeType, file);

        File uploaded = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink")
                .execute();

        // Set the file permissions to 'anyone with the link can write'
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("writer");
        driveService.permissions().create(uploaded.getId(), permission).execute();

        // Return the folder link
        String folderLink = "https://drive.google.com/drive/folders/" + folderId;
        System.out.println("Folder ID: " + folderId);
        return folderLink;
    }
}
