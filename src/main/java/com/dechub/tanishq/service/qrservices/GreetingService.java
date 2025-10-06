package com.dechub.tanishq.service.qrservices;

import com.dechub.tanishq.dto.qrcode.Greeting;
import com.dechub.tanishq.dto.qrcode.GreetingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class GreetingService {

    @Autowired
    @Qualifier("greetingSheetsService")   // use the greetings-specific Sheets bean
    private GoogleSheetsService sheets;

    @Autowired
    @Qualifier("greetingDriveService")    // use the greetings-specific Drive bean
    private GoogleDriveService drive;

    // Create greeting → adds a new row with empty placeholders
    public String createGreeting() throws Exception {
        Greeting greeting = new Greeting();
        greeting.setGoogleDriveFileId("");
        sheets.appendGreeting(greeting);
        return greeting.getUniqueId();
    }

    // Upload video + optional name/message in same request
    public void uploadVideoAndUpdateGreeting(String uniqueId,
                                             MultipartFile videoFile,
                                             String name,
                                             String message) throws Exception {
        // Upload video to Google Drive
        String fileName = uniqueId + "-" + videoFile.getOriginalFilename();
        String fileId = drive.uploadFile(fileName, videoFile.getInputStream(), videoFile.getContentType());

        // Update status, fileId, playback URL, timestamp
        sheets.markUploaded(uniqueId, fileId);

        // If name/message provided, update them too
        if ((name != null && !name.isEmpty()) || (message != null && !message.isEmpty())) {
            sheets.updateNameAndMessage(uniqueId, name, message);
        }
    }

    // View greeting info (returns hasVideo, status, fileId, playbackUrl, timestamp, name, message)
    public Optional<GreetingInfo> getGreetingInfo(String uniqueId) throws Exception {
        return sheets.getGreetingInfo(uniqueId);
    }
}
