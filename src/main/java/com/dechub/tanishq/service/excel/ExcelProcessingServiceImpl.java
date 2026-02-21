package com.dechub.tanishq.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ExcelProcessingService
 * Handles all Apache POI Excel file operations
 */
@Slf4j
@Service
public class ExcelProcessingServiceImpl implements ExcelProcessingService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<List<Object>> readExcelFile(MultipartFile file) throws IOException {
        List<List<Object>> data = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // Determine header columns
            int headerCols = 0;
            Row header = sheet.getRow(0);
            if (header != null) {
                headerCols = header.getLastCellNum();
            }

            // Process each row starting from index 1 (skip header)
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                List<Object> rowData = new ArrayList<>();
                int lastCell = headerCols > 0 ? headerCols : row.getLastCellNum();

                for (int c = 0; c < lastCell; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String text = formatter.formatCellValue(cell).trim();
                    rowData.add(text);
                }

                // Skip completely blank rows
                if (!rowData.stream().allMatch(obj -> obj.toString().trim().isEmpty())) {
                    data.add(rowData);
                }
            }
        }

        log.debug("Read {} rows from Excel file", data.size());
        return data;
    }

    @Override
    public List<List<Object>> addMetadataAndEventId(List<List<Object>> data, String eventId, boolean isAttendees) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        List<List<Object>> newData = new ArrayList<>();

        for (List<Object> row : data) {
            List<Object> newRow = new ArrayList<>();

            if (isAttendees) {
                // Handle attendee format: Name(0), Phone(1), FirstTime(2), RsoName(3)
                // Note: Like column might not be present, so we'll leave it empty or handle it specially
                String name = safeString(row, 0);
                String phone = normalizePhone(safeString(row, 1));
                String firstRaw = safeString(row, 2); // FirstTime is now at index 2
                String rsoName = safeString(row, 3);  // RsoName at index 3
                String like = safeString(row, 4);     // Like at index 4 if present, otherwise empty

                // Skip blank rows
                if (name.isEmpty() && phone.isEmpty()) continue;

                // Validate phone number
                if (!validatePhoneNumber(phone)) {
                    log.warn("Skipping attendee with invalid phone: {}", phone);
                    continue;
                }

                boolean firstTime = parseBooleanLenient(firstRaw);

                newRow.add(eventId);      // Event ID
                newRow.add(name);         // Name
                newRow.add(phone);        // Phone (normalized)
                newRow.add(like);         // Like (empty if not provided)
                newRow.add(firstTime);    // First time
                newRow.add(timestamp);    // Created At
                newRow.add(true);         // isUploadedFromExcel
                newRow.add(rsoName);      // RSO Name (added for reference)

            } else {
                // Invitees format: Name(0), Phone(1)
                String name = safeString(row, 0);
                String phone = normalizePhone(safeString(row, 1));

                // Skip blank rows
                if (name.isEmpty() && phone.isEmpty()) continue;

                // Validate phone number
                if (!validatePhoneNumber(phone)) {
                    log.warn("Skipping invitee with invalid phone: {}", phone);
                    continue;
                }

                newRow.add(name);         // Name
                newRow.add(phone);        // Phone (normalized)
                newRow.add(timestamp);    // Created At
                newRow.add(eventId);      // Event ID
            }

            newData.add(newRow);
        }

        log.debug("Processed {} rows with eventId: {}", newData.size(), eventId);
        return newData;
    }

    @Override
    public boolean validatePhoneNumber(String phone) {
        if (phone == null) return false;
        String normalized = phone.replaceAll("[^0-9]", "");
        return normalized.length() == 10 && normalized.matches("\\d{10}");
    }

    @Override
    public boolean parseBooleanLenient(String value) {
        if (value == null) return false;
        String lower = value.trim().toLowerCase();
        return lower.equals("true") || lower.equals("yes") || lower.equals("y") || lower.equals("1");
    }

    @Override
    public String safeString(List<Object> row, int index) {
        if (row == null || index < 0 || index >= row.size()) return "";
        Object value = row.get(index);
        return value == null ? "" : value.toString().trim();
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "");
    }

    @Override
    public List<Map<String, String>> readAttendeesExcel(java.io.InputStream inputStream) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // Read header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                log.warn("Excel file has no header row");
                return data;
            }

            List<String> headers = new ArrayList<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String headerName = formatter.formatCellValue(cell).trim();
                headers.add(headerName);
            }

            // Process data rows
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                boolean hasData = false;

                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = formatter.formatCellValue(cell).trim();
                    
                    String headerName = headers.get(c);
                    rowData.put(headerName, value);
                    
                    if (!value.isEmpty()) {
                        hasData = true;
                    }
                }

                // Only add rows that have at least some data
                if (hasData) {
                    data.add(rowData);
                }
            }
        }

        log.debug("Read {} attendee rows from Excel file", data.size());
        return data;
    }
}
