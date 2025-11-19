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
 * Import invitees from Google Sheets -> MySQL table event_invitees.
 * Expects sheet tab with columns: name, contact, createdAt, event_id (header row at row 1).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InviteeImportService {

    private final NamedParameterJdbcTemplate jdbc;
    private final GSheetUserDetailsUtil gsheetUtil;

    // batch size for DB write
    private static final int BATCH_SIZE = 1000;

    /**
     * Import invitees from the given spreadsheet id. Range assumed: Sheet1!A2:D
     */
    public void importInviteesFromSheet(String spreadsheetId) {
        log.info("InviteeImportService: importing invitees from sheet {}", spreadsheetId);
        try {
            // fetch rows (A2:D) — adjust range if you have more columns
            ValueRange vr = gsheetUtil.getValues(spreadsheetId, "Sheet1!A2:D");
            List<List<Object>> rows = vr == null ? Collections.emptyList() : vr.getValues();

            if (rows == null || rows.isEmpty()) {
                log.info("InviteeImportService: no invitee rows found");
                return;
            }

            List<Map<String,Object>> batch = new ArrayList<>();
            int skipped = 0;

            for (int i = 0; i < rows.size(); i++) {
                List<Object> r = rows.get(i);
                // Expect at least 4 columns — but be tolerant
                String name = safeGet(r, 0);
                String contact = safeGet(r, 1).replaceAll("[^0-9]", ""); // keep digits only
                String createdAtRaw = safeGet(r, 2);
                String eventId = safeGet(r, 3);

                if (eventId.isBlank() || contact.isBlank() || name.isBlank()) {
                    skipped++;
                    log.debug("InviteeImportService: skipping row {} missing required fields: {}", i+2, r);
                    continue;
                }

                // parse createdAt to Timestamp if possible
                Timestamp createdAt = parseTimestamp(createdAtRaw);

                Map<String,Object> params = new HashMap<>();
                params.put("event_id", eventId.trim());
                params.put("name", name.trim());
                params.put("contact", contact.trim());
                params.put("created_at", createdAt);
                params.put("is_uploaded_from_excel", true); // sheet import => true
                params.put("rso_name", null);
                params.put("like_text", null);

                batch.add(params);
            }

            log.info("InviteeImportService: prepared {} rows, skipped {}", batch.size(), skipped);

            if (batch.isEmpty()) {
                log.info("InviteeImportService: nothing to insert");
                return;
            }

            // SQL: insert or ignore duplicates based on unique key (event_id, name, contact)
            String sql = "INSERT INTO event_invitees (" +
                    "event_id, name, contact, created_at, is_uploaded_from_excel, rso_name, like_text" +
                    ") VALUES (" +
                    ":event_id, :name, :contact, :created_at, :is_uploaded_from_excel, :rso_name, :like_text" +
                    ") ON DUPLICATE KEY UPDATE " +
                    // no-op update to keep existing row; you can change to update fields if you want
                    "created_at = VALUES(created_at)";

            int total = 0;
            for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
                int end = Math.min(batch.size(), i + BATCH_SIZE);
                List<Map<String,Object>> chunk = batch.subList(i, end);
                int[] res = jdbc.batchUpdate(sql, chunk.toArray(new Map[0]));
                int affected = Arrays.stream(res).sum();
                total += affected;
                log.info("InviteeImportService: inserted/updated chunk {}..{} -> {} rows affected", i, end-1, affected);
            }

            log.info("InviteeImportService: import finished. totalAffected = {}", total);

        } catch (Exception e) {
            log.error("InviteeImportService: import failed", e);
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
        // try parse ISO or common formats; fallback to current timestamp
        try {
            // Some sheets show "2025/11/10 17:19:58" so try replacing slashes and parse
            String normalized = s.trim().replace('/', '-');
            // try standard pattern "yyyy-MM-dd HH:mm:ss"
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(normalized, fmt);
            return Timestamp.valueOf(ldt);
        } catch (Exception ignored) {}

        try { // try Instant.parse for ISO strings
            Instant inst = Instant.parse(s);
            return Timestamp.from(inst);
        } catch (Exception ignored) {}

        // Last attempt: epoch millis
        try {
            long v = Long.parseLong(s);
            if (v > 1_000_000_000_000L) return new Timestamp(v);
            else return new Timestamp(v * 1000L);
        } catch (Exception ignored) {}

        // fallback: null
        return null;
    }
}
