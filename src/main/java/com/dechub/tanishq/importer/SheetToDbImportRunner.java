package com.dechub.tanishq.importer;

import com.dechub.tanishq.dto.ExcelStoreDTO;
import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resilient one-shot import runner: reads stores & events from sheets and upserts into events_master.
 * - safe created_at parsing
 * - chunked batch updates
 * - skips bad rows instead of failing startup
 */
@Component
@Profile("mysql")
public class SheetToDbImportRunner implements CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(SheetToDbImportRunner.class);
    private final GSheetUserDetailsUtil gsheet;
    private final NamedParameterJdbcTemplate jdbc;

    // chunk size for batchUpdate (tune if needed)
    private static final int BATCH_SIZE = 1000;

    public SheetToDbImportRunner(GSheetUserDetailsUtil gsheet, NamedParameterJdbcTemplate jdbc) {
        this.gsheet = gsheet;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("SheetToDbImportRunner: fetching store list from Sheets...");
            List<ExcelStoreDTO> stores = gsheet.getData();
            if (stores == null || stores.isEmpty()) {
                log.warn("SheetToDbImportRunner: no stores found in sheet - nothing to import.");
                return;
            }

            List<String> storeCodes = stores.stream()
                    .map(ExcelStoreDTO::getStoreCode)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (storeCodes.isEmpty()) {
                log.warn("SheetToDbImportRunner: no valid store codes found - aborting.");
                return;
            }

            log.info("SheetToDbImportRunner: fetching events for {} stores...", storeCodes.size());
            List<Map<String, Object>> events = gsheet.getEventsForStores(storeCodes);

            if (events == null || events.isEmpty()) {
                log.warn("SheetToDbImportRunner: no events found to import.");
                return;
            }

            // log first row keys to help debug header name mismatches
            log.info("SheetToDbImportRunner: example event keys: {}", events.get(0).keySet());

            final String sql = "INSERT INTO events_master (" +
                    "event_id, created_at, store_code, region, event_type, event_sub_type, event_name, rso, start_date, image, invitees, attendees, completed_events_link, community, location, is_attendees_uploaded, sale, advance, ghs_rga, gmb, drive_link, diamond_awareness, ghs_flag" +
                    ") VALUES (" +
                    ":event_id, :created_at, :store_code, :region, :event_type, :event_sub_type, :event_name, :rso, :start_date, :image, :invitees, :attendees, :completed_events_link, :community, :location, :is_attendees_uploaded, :sale, :advance, :ghs_rga, :gmb, :drive_link, :diamond_awareness, :ghs_flag" +
                    ") ON DUPLICATE KEY UPDATE " +
                    "store_code = VALUES(store_code), region = VALUES(region), event_type = VALUES(event_type), event_sub_type = VALUES(event_sub_type), event_name = VALUES(event_name), rso = VALUES(rso), start_date = VALUES(start_date), image = VALUES(image), invitees = VALUES(invitees), attendees = VALUES(attendees), completed_events_link = VALUES(completed_events_link), community = VALUES(community), location = VALUES(location), is_attendees_uploaded = VALUES(is_attendees_uploaded), sale = VALUES(sale), advance = VALUES(advance), ghs_rga = VALUES(ghs_rga), gmb = VALUES(gmb), drive_link = VALUES(drive_link), diamond_awareness = VALUES(diamond_awareness), ghs_flag = VALUES(ghs_flag)";

            List<Map<String, Object>> prepared = new ArrayList<>(events.size());
            int skipped = 0;
            for (Map<String, Object> e : events) {
                try {
                    Map<String, Object> p = new HashMap<>();
                    String eventId = Optional.ofNullable(e.getOrDefault("EventId", e.get("Id"))).map(Object::toString).orElse(null);
                    if (eventId == null || eventId.trim().isEmpty()) {
                        // skip if no event id found
                        skipped++;
                        log.debug("Skipping event row because missing eventId: {}", e);
                        continue;
                    }
                    p.put("event_id", eventId.trim());

                    // parse createdAt safely
                    Timestamp createdAtTs = parseCreatedAt(e.getOrDefault("CreatedAt", e.get("createdAt")));
                    p.put("created_at", createdAtTs);

                    p.put("store_code", Optional.ofNullable(e.get("StoreCode")).map(Object::toString).orElse("").trim());
                    p.put("region", Optional.ofNullable(e.get("Region")).map(Object::toString).orElse(null));
                    p.put("event_type", Optional.ofNullable(e.get("EventType")).map(Object::toString).orElse(null));
                    p.put("event_sub_type", Optional.ofNullable(e.get("EventSubType")).map(Object::toString).orElse(null));
                    p.put("event_name", Optional.ofNullable(e.get("EventName")).map(Object::toString).orElse(null));
                    p.put("rso", Optional.ofNullable(e.get("RSO")).map(Object::toString).orElse(null));

                    // start_date (date-only)
                    String sd = Optional.ofNullable(e.get("StartDate")).map(Object::toString).orElse("").trim();
                    if (!sd.isEmpty()) {
                        try {
                            LocalDate ld = LocalDate.parse(sd.replaceAll("/", "-"));
                            p.put("start_date", Date.valueOf(ld));
                        } catch (Exception ex) {
                            p.put("start_date", null);
                        }
                    } else {
                        p.put("start_date", null);
                    }

                    p.put("image", Optional.ofNullable(e.get("Image")).map(Object::toString).orElse(null));
                    p.put("invitees", safeInt(e.get("Invitees")));
                    p.put("attendees", safeInt(e.get("Attendees")));
                    p.put("completed_events_link", Optional.ofNullable(e.get("completedEvents")).map(Object::toString).orElse(null));
                    p.put("community", Optional.ofNullable(e.get("Community")).map(Object::toString).orElse(null));
                    p.put("location", Optional.ofNullable(e.get("location")).map(Object::toString).orElse(null));
                    p.put("is_attendees_uploaded", safeBoolean(e.get("isAttendeesUploaded")));
                    p.put("sale", safeDecimal(e.get("sale")));
                    p.put("advance", safeDecimal(e.get("advance")));
                    p.put("ghs_rga", safeDecimal(e.get("ghs/rga")));
                    p.put("gmb", safeDecimal(e.get("gmb")));
                    p.put("drive_link", Optional.ofNullable(e.get("Drive link")).map(Object::toString).orElse(null));
                    p.put("diamond_awareness", safeBoolean(e.get("Diamond Awareness")));
                    p.put("ghs_flag", safeBoolean(e.get("GHS")));

                    prepared.add(p);
                } catch (Exception rowEx) {
                    skipped++;
                    log.warn("Skipping row due to parsing error: {} -- error: {}", e, rowEx.getMessage());
                }
            }

            log.info("SheetToDbImportRunner: prepared {} rows, skipped {} bad rows.", prepared.size(), skipped);

            // chunked batch updates
            int totalInserted = 0;
            for (int i = 0; i < prepared.size(); i += BATCH_SIZE) {
                int end = Math.min(prepared.size(), i + BATCH_SIZE);
                List<Map<String, Object>> chunk = prepared.subList(i, end);
                try {
                    int[] r = jdbc.batchUpdate(sql, chunk.toArray(new Map[0]));
                    int successCount = Arrays.stream(r).sum();
                    totalInserted += successCount;
                    log.info("Inserted/updated chunk {}..{} -> {} rows affected", i, end - 1, successCount);
                } catch (Exception chunkEx) {
                    log.error("Failed inserting chunk {}..{} ({} rows). Error: {}", i, end - 1, chunk.size(), chunkEx.getMessage());
                    // optionally you could try per-row here, but we skip to keep startup healthy
                }
            }

            log.info("SheetToDbImportRunner: import finished. total prepared={}, totalAffected={}, skipped={}",
                    prepared.size(), totalInserted, skipped);

        } catch (Exception ex) {
            // do NOT throw - keep app running; log full stacktrace
            log.error("SheetToDbImportRunner: unexpected failure during import", ex);
        }
    }

    // tries several formats; returns current timestamp if parsing fails
    private static Timestamp parseCreatedAt(Object raw) {
        if (raw == null) return new Timestamp(System.currentTimeMillis());
        String s = raw.toString().trim();
        if (s.isEmpty()) return new Timestamp(System.currentTimeMillis());

        // Common SQL timestamp format
        try {
            if (s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*")) {
                return Timestamp.valueOf(s.split("\\.")[0]);
            }
        } catch (Exception ignored) {}

        // ISO datetime
        try {
            Instant inst = Instant.parse(s); // e.g. 2023-06-01T10:15:30Z
            return Timestamp.from(inst);
        } catch (Exception ignored) {}

        // parse with OffsetDateTime or LocalDateTime with common patterns
        String[] patterns = new String[] {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy/MM/dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss"
        };
        for (String p : patterns) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(p).withZone(ZoneOffset.UTC);
                LocalDateTime ldt = LocalDateTime.parse(s, fmt);
                return Timestamp.valueOf(ldt);
            } catch (Exception ignored) {}
        }

        // epoch millis?
        try {
            long v = Long.parseLong(s);
            // heuristic: milliseconds ( >= 1e12 ) vs seconds
            if (v > 100000000000L) {
                return new Timestamp(v);
            } else {
                return new Timestamp(v * 1000L);
            }
        } catch (Exception ignored) {}

        // date only
        try {
            LocalDate d = LocalDate.parse(s);
            return Timestamp.valueOf(d.atStartOfDay());
        } catch (Exception ignored) {}

        // fallback: now
        return new Timestamp(System.currentTimeMillis());
    }

    private static Integer safeInt(Object o) {
        if (o == null) return 0;
        try { return Integer.parseInt(o.toString()); } catch (Exception e) {
            try { return (int) Double.parseDouble(o.toString()); } catch (Exception ex) { return 0; }
        }
    }

    private static Double safeDecimal(Object o) {
        if (o == null) return 0d;
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0d; }
    }

    private static Boolean safeBoolean(Object o) {
        if (o == null) return false;
        String s = o.toString().trim().toLowerCase();
        return s.equals("true") || s.equals("yes") || s.equals("1") || s.equals("y");
    }
}
