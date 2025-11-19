package com.dechub.tanishq.importer;

import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Import attendees from Google Sheets into event_attendees table.
 * Expected sheet tab headers (row1): Event_id, Name, Contact, Like, first Time, Created At, isUploadedFromExcel, Rso Name
 * Data rows from A2:H...
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendeeImportService {

    private final NamedParameterJdbcTemplate jdbc;
    private final GSheetUserDetailsUtil gsheetUtil;

    private static final int BATCH_SIZE = 1000;

    /**
     * Import attendees from the given spreadsheet id
     */
    public void importAttendeesFromSheet(String spreadsheetId) {
        log.info("AttendeeImportService: starting import from sheet {}", spreadsheetId);
        try {
            // adjust range if your data has more columns or a different sheet name
            ValueRange vr = gsheetUtil.getValues(spreadsheetId, "Sheet1!A2:H");
            List<List<Object>> rows = vr == null ? Collections.emptyList() : vr.getValues();

            if (rows == null || rows.isEmpty()) {
                log.info("AttendeeImportService: no rows found");
                return;
            }

            List<Map<String,Object>> batch = new ArrayList<>();
            int skipped = 0;
            for (int i = 0; i < rows.size(); i++) {
                List<Object> r = rows.get(i);
                String eventId = safeGet(r, 0);
                String name = safeGet(r, 1);
                String contact = safeGet(r, 2).replaceAll("[^0-9]", ""); // keep digits only
                String likedRaw = safeGet(r, 3);
                Boolean liked = parseLikeToBoolean(likedRaw);
                String firstTimeRaw = safeGet(r, 4);
                String createdAtRaw = safeGet(r, 5);
                String isUploadedRaw = safeGet(r, 6);
                String rsoName = safeGet(r, 7);

                if (eventId.isBlank() || name.isBlank() || contact.isBlank()) {
                    skipped++;
                    log.debug("AttendeeImportService: skipping row {} missing required fields: {}", i+2, r);
                    continue;
                }

                Timestamp createdAt = parseTimestamp(createdAtRaw);
                boolean firstTime = parseBoolean(firstTimeRaw);
                boolean isUploaded = parseBoolean(isUploadedRaw);

                Map<String,Object> params = new HashMap<>();
                params.put("event_id", eventId.trim());
                params.put("name", name.trim());
                params.put("contact", contact.trim());
                params.put("liked", liked ? 1 : 0);
                params.put("first_time", firstTime);
                params.put("created_at", createdAt);
                params.put("is_uploaded_from_excel", isUploaded);
                params.put("rso_name", rsoName.isBlank() ? null : rsoName.trim());

                batch.add(params);
            }

            log.info("AttendeeImportService: prepared {} rows, skipped {}", batch.size(), skipped);

            if (batch.isEmpty()) {
                log.info("AttendeeImportService: nothing to insert");
                return;
            }

            String sql = "INSERT INTO event_attendees (" +
                    "event_id, name, contact, liked, first_time, created_at, is_uploaded_from_excel, rso_name" +
                    ") VALUES (" +
                    ":event_id, :name, :contact, :liked, :first_time, :created_at, :is_uploaded_from_excel, :rso_name" +
                    ") ON DUPLICATE KEY UPDATE " +
                    // keep a no-op or update created_at if you wish
                    "created_at = VALUES(created_at)";

            int total = 0;
            for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
                int end = Math.min(batch.size(), i + BATCH_SIZE);
                List<Map<String,Object>> chunk = batch.subList(i, end);
                int[] res = jdbc.batchUpdate(sql, chunk.toArray(new Map[0]));
                int affected = Arrays.stream(res).sum();
                total += affected;
                log.info("AttendeeImportService: chunk {}..{} -> {} rows affected", i, end - 1, affected);
            }

            log.info("AttendeeImportService: import finished. totalAffected={}", total);

        } catch (Exception e) {
            log.error("AttendeeImportService: import failed", e);
            throw new RuntimeException(e);
        }
    }

    private static String safeGet(List<Object> r, int idx) {
        if (r == null || idx < 0 || idx >= r.size()) return "";
        Object o = r.get(idx);
        return o == null ? "" : o.toString().trim();
    }

    private static Timestamp parseTimestamp(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            String normalized = s.trim().replace('/', '-');
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(normalized, fmt);
            return Timestamp.valueOf(ldt);
        } catch (Exception ignored) {}
        try {
            Instant inst = Instant.parse(s);
            return Timestamp.from(inst);
        } catch (Exception ignored) {}
        try {
            long v = Long.parseLong(s);
            if (v > 1_000_000_000_000L) return new Timestamp(v);
            else return new Timestamp(v * 1000L);
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean parseBoolean(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.equals("true") || t.equals("yes") || t.equals("1") || t.equals("y");
    }

    private static Boolean parseLikeToBoolean(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.equals("true") || t.equals("yes") || t.equals("y") || t.equals("1");
    }
}
