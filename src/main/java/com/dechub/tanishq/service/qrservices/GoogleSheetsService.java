package com.dechub.tanishq.service.qrservices;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service("greetingSheetsService")
public class GoogleSheetsService {

    @Value("${dechub.tanishq.greeting.sheet.key.filepath}")
    private String keyPath;

    @Value("${dechub.tanishq.greeting.sheet.service.account}")
    private String serviceAccountEmail;

    @Value("${dechub.tanishq.greeting.sheet.id}")
    private String greetingSheetId;


    private static final String SHEET_NAME = "Sheet1";

    @PostConstruct
    public void checkKey() throws Exception {
        File f = new File(keyPath);
        if (!f.exists()) {
            throw new java.io.FileNotFoundException("Greeting SA key not found: " + f.getAbsolutePath());
        }
    }

    // GoogleSheetsService.java
    public void appendGreeting(com.dechub.tanishq.dto.qrcode.Greeting greeting) throws Exception {
        Sheets sheetsService = getSheetsService();
        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(Arrays.asList(
                        greeting.getUniqueId(), // A qrId
                        "PENDING",              // B status
                        "",                     // C videoFileId
                        "",                     // D videoPlaybackUrl
                        "",                     // E submissionTimestamp
                        "",                     // F name
                        ""                      // G message
                )));
        sheetsService.spreadsheets().values()
                .append(greetingSheetId, SHEET_NAME, body)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
    }


    private Sheets getSheetsService() throws Exception {
        File f = new File(keyPath);
        try (FileInputStream in = new FileInputStream(f)) {
            PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
                    SecurityUtils.getPkcs12KeyStore(), in,
                    "notasecret", "privatekey", "notasecret");

            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(new NetHttpTransport())
                    .setJsonFactory(JacksonFactory.getDefaultInstance())
                    .setServiceAccountId(serviceAccountEmail)
                    .setServiceAccountScopes(Collections.singleton(SheetsScopes.SPREADSHEETS))
                    .setServiceAccountPrivateKey(privateKey)
                    .build();

            return new Sheets.Builder(new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("Tanishq Greetings")
                    .build();
        }
    }

    public void updateDriveFileId(String uniqueId, String driveFileId) throws Exception {
        Sheets sheetsService = getSheetsService();
        String readRange = SHEET_NAME + "!A:A";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(greetingSheetId, readRange).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) throw new IOException("Spreadsheet is empty.");

        int rowIndex = -1;
        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (!row.isEmpty() && uniqueId.equals(String.valueOf(row.get(0)))) {
                rowIndex = i + 1;  // 1-based
                break;
            }
        }
        if (rowIndex == -1) throw new IOException("Unique ID not found in the spreadsheet.");

        String updateRange = SHEET_NAME + "!B" + rowIndex;
        ValueRange updateBody = new ValueRange()
                .setValues(Collections.singletonList(Collections.singletonList(driveFileId)));

        sheetsService.spreadsheets().values()
                .update(greetingSheetId, updateRange, updateBody)
                .setValueInputOption("RAW")
                .execute();
    }


    // Add this new method in GoogleSheetsService.java

    public void markUploaded(String uniqueId, String fileId) throws Exception {
        Sheets sheetsService = getSheetsService();

        // 1) find row index by qrId in column A
        String readRange = SHEET_NAME + "!A:A";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(greetingSheetId, readRange).execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new IOException("Spreadsheet is empty.");
        }

        int rowIndex = -1;
        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (!row.isEmpty() && uniqueId.equals(String.valueOf(row.get(0)))) {
                rowIndex = i + 1; // 1-based
                break;
            }
        }
        if (rowIndex == -1) throw new IOException("Unique ID not found in the spreadsheet.");

        // 2) build playback URL + timestamp
        String playbackUrl = "https://drive.google.com/file/d/" + fileId + "/view?usp=drivesdk";
        String timestampIso = java.time.Instant.now().toString();

        // 3) update B..E in one shot
        String updateRange = SHEET_NAME + "!B" + rowIndex + ":E" + rowIndex;
        ValueRange updateBody = new ValueRange()
                .setValues(Collections.singletonList(Arrays.asList(
                        "UPLOADED",      // B status
                        fileId,          // C videoFileId
                        playbackUrl,     // D videoPlaybackUrl
                        timestampIso     // E submissionTimestamp
                )));

        sheetsService.spreadsheets().values()
                .update(greetingSheetId, updateRange, updateBody)
                .setValueInputOption("RAW")
                .execute();
    }

    public void updateNameAndMessage(String uniqueId, String name, String message) throws Exception {
        Sheets sheetsService = getSheetsService();
        // find row by qrId
        ValueRange response = sheetsService.spreadsheets().values()
                .get(greetingSheetId, SHEET_NAME + "!A:A").execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) throw new IOException("Spreadsheet is empty.");

        int rowIndex = -1;
        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (!row.isEmpty() && uniqueId.equals(String.valueOf(row.get(0)))) {
                rowIndex = i + 1; // 1-based
                break;
            }
        }
        if (rowIndex == -1) throw new IOException("Unique ID not found in the spreadsheet.");

        String range = SHEET_NAME + "!F" + rowIndex + ":G" + rowIndex;
        ValueRange updateBody = new ValueRange()
                .setValues(Collections.singletonList(Arrays.asList(
                        name != null ? name : "",
                        message != null ? message : ""
                )));

        sheetsService.spreadsheets().values()
                .update(greetingSheetId, range, updateBody)
                .setValueInputOption("RAW")
                .execute();
    }


    public Optional<com.dechub.tanishq.dto.qrcode.GreetingInfo> getGreetingInfo(String uniqueId) throws Exception {
        Sheets sheetsService = getSheetsService();
        ValueRange response = sheetsService.spreadsheets().values()
                .get(greetingSheetId, SHEET_NAME + "!A:G") // read A..G
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) return Optional.empty();

        for (List<Object> row : values) {
            if (!row.isEmpty() && uniqueId.equals(String.valueOf(row.get(0)))) {
                String status    = row.size() > 1 ? toStr(row.get(1)) : null; // B
                String fileId    = row.size() > 2 ? toStr(row.get(2)) : null; // C
                String playback  = row.size() > 3 ? toStr(row.get(3)) : null; // D
                String submitted = row.size() > 4 ? toStr(row.get(4)) : null; // E
                String name      = row.size() > 5 ? toStr(row.get(5)) : null; // F
                String message   = row.size() > 6 ? toStr(row.get(6)) : null; // G

                boolean hasVideo = "UPLOADED".equalsIgnoreCase(status)
                        && fileId != null && !fileId.isEmpty();

                return Optional.of(new com.dechub.tanishq.dto.qrcode.GreetingInfo(
                        hasVideo, status, fileId, playback, submitted, name, message
                ));
            }
        }
        return Optional.empty();
    }
    private String toStr(Object o) { return o == null ? null : o.toString(); }


}
