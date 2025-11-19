package com.dechub.tanishq.repository.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class JdbcStoreImportRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public JdbcStoreImportRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void clearStores() {
        jdbc.getJdbcTemplate().execute("TRUNCATE TABLE stores");
        log.info("🧹 Cleared stores table");
    }

    public void batchInsertStores(List<Map<String, Object>> batch) {
        if (batch == null || batch.isEmpty()) {
            log.info("batchInsertStores: nothing to insert");
            return;
        }

        // your INSERT SQL (use the same column names you already used)
        final String sql = "INSERT INTO stores (" +
                "store_code, region, level, store_name, store_address, city, state, country, zipcode, " +
                "phone_1, phone_2, email, latitude, longitude, opening_date, store_type, opening_time, closing_time, " +
                "manager_name, manager_phone, manager_email, maps_link, languages, parking, payment, " +
                "kakatiya_store, celeste_store, rating, number_of_ratings, is_collection, rhythm_collection" +
                ") VALUES (" +
                ":store_code, :region, :level, :store_name, :store_address, :city, :state, :country, :zipcode, " +
                ":phone_1, :phone_2, :email, :latitude, :longitude, :opening_date, :store_type, :opening_time, :closing_time, " +
                ":manager_name, :manager_phone, :manager_email, :maps_link, :languages, :parking, :payment, " +
                ":kakatiya_store, :celeste_store, :rating, :number_of_ratings, :is_collection, :rhythm_collection" +
                ") ON DUPLICATE KEY UPDATE " +
                "region = VALUES(region), level = VALUES(level), store_name = VALUES(store_name), store_address = VALUES(store_address), " +
                "city = VALUES(city), state = VALUES(state), country = VALUES(country), zipcode = VALUES(zipcode), " +
                "phone_1 = VALUES(phone_1), phone_2 = VALUES(phone_2), email = VALUES(email), latitude = VALUES(latitude), longitude = VALUES(longitude), " +
                "opening_date = VALUES(opening_date), store_type = VALUES(store_type), opening_time = VALUES(opening_time), closing_time = VALUES(closing_time), " +
                "manager_name = VALUES(manager_name), manager_phone = VALUES(manager_phone), manager_email = VALUES(manager_email), maps_link = VALUES(maps_link), " +
                "languages = VALUES(languages), parking = VALUES(parking), payment = VALUES(payment), kakatiya_store = VALUES(kakatiya_store), celeste_store = VALUES(celeste_store), " +
                "rating = VALUES(rating), number_of_ratings = VALUES(number_of_ratings), is_collection = VALUES(is_collection), rhythm_collection = VALUES(rhythm_collection)";

        int success = 0;
        int failed = 0;

        for (int i = 0; i < batch.size(); i++) {
            Map<String, Object> raw = batch.get(i);
            try {
                Map<String, Object> params = new HashMap<>();

                // normalize and map types
                params.put("store_code", optionalToUpper(raw.get("store_code")));
                params.put("region", safeToString(raw.get("region")));
                params.put("level", safeToString(raw.get("level")));
                params.put("store_name", safeToString(raw.get("store_name")));
                params.put("store_address", safeToString(raw.get("store_address"))); // note column name
                params.put("city", safeToString(raw.get("city")));
                params.put("state", safeToString(raw.get("state")));
                params.put("country", safeToString(raw.get("country")));
                params.put("zipcode", safeToString(raw.get("zipcode")));
                params.put("phone_1", safeToString(raw.get("phone_1")));
                params.put("phone_2", safeToString(raw.get("phone_2")));
                params.put("email", safeToString(raw.get("email")));
                params.put("latitude", toDoubleOrNull(raw.get("latitude")));
                params.put("longitude", toDoubleOrNull(raw.get("longitude")));
                params.put("opening_date", toSqlDateOrNull(raw.get("opening_date"))); // returns java.sql.Date or null
                params.put("store_type", safeToString(raw.get("store_type")));
                params.put("opening_time", safeToString(raw.get("opening_time")));
                params.put("closing_time", safeToString(raw.get("closing_time")));
                params.put("manager_name", safeToString(raw.get("manager_name")));
                params.put("manager_phone", safeToString(raw.get("manager_phone")));
                params.put("manager_email", safeToString(raw.get("manager_email")));
                params.put("maps_link", safeToString(raw.get("maps_link")));

                // lists -> json text (if your column is text)
                params.put("languages", listToJsonOrNull(raw.get("languages")));
                params.put("parking", listToJsonOrNull(raw.get("parking")));
                params.put("payment", listToJsonOrNull(raw.get("payment")));

                params.put("kakatiya_store", toBooleanOrFalse(raw.get("kakatiya_store")));
                params.put("celeste_store", toBooleanOrFalse(raw.get("celeste_store")));
                params.put("rating", toDoubleOrNull(raw.get("rating")));
                params.put("number_of_ratings", toIntegerOrNull(raw.get("number_of_ratings")));
                params.put("is_collection", toBooleanOrFalse(raw.get("is_collection")));
                params.put("rhythm_collection", toBooleanOrFalse(raw.get("rhythm_collection")));

                NamedParameterJdbcTemplate npjt = this.jdbc; // field in class
                npjt.update(sql, params);
                success++;
            } catch (Exception ex) {
                failed++;
                log.error("Failed inserting store row idx={} code={} error={} data={}",
                        i,
                        raw != null ? raw.get("store_code") : "<null>",
                        ex.getMessage(),
                        raw);
                // continue with next row
            }
        }

        log.info("batchInsertStores finished: success={}, failed={}", success, failed);
    }

    // --- helper methods (add these to same class) ---
    private String safeToString(Object o) {
        if (o == null) return "";
        return o.toString().trim();
    }

    private String optionalToUpper(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s.toUpperCase();
    }

    private Double toDoubleOrNull(Object o) {
        if (o == null) return null;
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toIntegerOrNull(Object o) {
        if (o == null) return null;
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            try {
                return (int) Math.round(Double.parseDouble(o.toString()));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private Boolean toBooleanOrFalse(Object o) {
        if (o == null) return false;
        String s = o.toString().trim().toLowerCase();
        return s.equals("true") || s.equals("yes") || s.equals("1") || s.equals("y");
    }

    private java.sql.Date toSqlDateOrNull(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        if (s.isEmpty()) return null;
        // try common patterns
        String[] patterns = new String[]{"dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "dd-MM-yyyy HH:mm:ss"};
        for (String p : patterns) {
            try {
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern(p);
                if (p.contains("HH")) {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s, fmt);
                    return java.sql.Date.valueOf(ldt.toLocalDate());
                } else {
                    java.time.LocalDate ld = java.time.LocalDate.parse(s, fmt);
                    return java.sql.Date.valueOf(ld);
                }
            } catch (Exception ignored) {
            }
        }
        // try ISO
        try {
            java.time.LocalDate ld = java.time.LocalDate.parse(s);
            return java.sql.Date.valueOf(ld);
        } catch (Exception ignored) {
        }
        // final fallback: null
        return null;
    }

    private String listToJsonOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof String) return o.toString();
        try {
            // If you have Jackson ObjectMapper available, use it (mapper field)
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            return o.toString();
        }
    }
}