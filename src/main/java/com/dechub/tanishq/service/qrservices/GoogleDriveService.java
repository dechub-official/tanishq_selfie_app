package com.dechub.tanishq.service.qrservices;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Collections;

@Service("greetingDriveService")
public class GoogleDriveService {

    @Value("${dechub.tanishq.greeting.drive.key.filepath}")
    private String keyPath;

    @Value("${dechub.tanishq.greeting.drive.service.account}")
    private String serviceAccountEmail;

    @Value("${dechub.tanishq.greeting.drive.folder.id}")
    private String driveFolderId;

    @PostConstruct
    public void checkKey() throws Exception {
        File f = new File(keyPath);
        if (!f.exists()) {
            throw new java.io.FileNotFoundException("Event SA key not found: " + f.getAbsolutePath());
        }
    }

    private Drive getDriveService() throws Exception {
        try (FileInputStream in = new FileInputStream(new File(keyPath))) {
            PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
                    SecurityUtils.getPkcs12KeyStore(), in, "notasecret", "privatekey", "notasecret");

            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(new NetHttpTransport())
                    .setJsonFactory(JacksonFactory.getDefaultInstance())
                    .setServiceAccountId(serviceAccountEmail)
                    .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE))
                    .setServiceAccountPrivateKey(privateKey)
                    .build();

            return new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("Tanishq Greetings")
                    .build();
        }
    }

    public String uploadFile(String fileName, InputStream inputStream, String contentType) throws Exception {
        Drive driveService = getDriveService();

        com.google.api.services.drive.model.File meta = new com.google.api.services.drive.model.File();
        meta.setName(fileName);
        // upload to shared drive root (your 0ACZ… ID) OR to a subfolder ID
        meta.setParents(Collections.singletonList(driveFolderId));

        com.google.api.services.drive.model.File uploaded =
                driveService.files()
                        .create(meta, new InputStreamContent(contentType, inputStream))
                        .setFields("id,parents")
                        .setSupportsAllDrives(true)      // <-- IMPORTANT for shared drives
                        .execute();

        return uploaded.getId();
    }
}
