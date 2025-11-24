package com.dechub.tanishq.service.excel;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for Excel processing operations
 * Single Responsibility: Handle all Apache POI Excel file operations
 */
public interface ExcelProcessingService {

    /**
     * Read Excel file and return data rows (excluding header)
     */
    List<List<Object>> readExcelFile(MultipartFile file) throws IOException;

    /**
     * Add metadata and event ID to Excel data
     */
    List<List<Object>> addMetadataAndEventId(List<List<Object>> data, String eventId, boolean isAttendees);

    /**
     * Validate phone number format
     */
    boolean validatePhoneNumber(String phone);

    /**
     * Parse boolean from Excel cell value
     */
    boolean parseBooleanLenient(String value);

    /**
     * Sanitize string value from Excel cell
     */
    String safeString(List<Object> row, int index);

    /**
     * Read attendees Excel file with headers and return as list of maps
     * Expected columns: Event_id, Name, Contact, Like, first Time, Created At, isUploadedFromExcel, Rso Name
     */
    List<Map<String, String>> readAttendeesExcel(java.io.InputStream inputStream) throws IOException;
}
