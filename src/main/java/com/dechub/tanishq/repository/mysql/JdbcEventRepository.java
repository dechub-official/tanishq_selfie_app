package com.dechub.tanishq.repository.mysql;

import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.repository.EventRepository;
import com.dechub.tanishq.util.ResponseDataDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Profile("mysql")
@Primary
public class JdbcEventRepository implements EventRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcEventRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private RowMapper<Map<String,Object>> eventRowMapper = (rs, i) -> {
        Map<String,Object> m = new HashMap<>();
        // map expected keys used in service (use same header keys)
        m.put("StoreCode", rs.getString("store_code"));
        m.put("Id", rs.getString("event_id"));
        m.put("EventId", rs.getString("event_id"));
        m.put("EventType", rs.getString("event_type"));
        m.put("EventSubType", rs.getString("event_sub_type"));
        m.put("EventName", rs.getString("event_name"));
        m.put("RSO", rs.getString("rso"));
        m.put("StartDate", rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate().toString() : "");
        m.put("Image", rs.getString("image"));
        m.put("Invitees", rs.getInt("invitees"));
        m.put("Attendees", rs.getInt("attendees"));
        m.put("completedEvents", rs.getString("completed_events_link"));
        m.put("Community", rs.getString("community"));
        m.put("location", rs.getString("location"));
        m.put("isAttendeesUploaded", rs.getBoolean("is_attendees_uploaded"));
        m.put("sale", rs.getBigDecimal("sale"));
        m.put("advance", rs.getBigDecimal("advance"));
        m.put("ghs/rga", rs.getBigDecimal("ghs_rga"));
        m.put("gmb", rs.getBigDecimal("gmb"));
        m.put("Drive link", rs.getString("drive_link"));
        m.put("Diamond Awareness", rs.getBoolean("diamond_awareness"));
        m.put("GHS", rs.getBoolean("ghs_flag"));
        return m;
    };

    @Override
    public List<Map<String, Object>> getEventsForStores(List<String> storeCodes) throws Exception {
        if (storeCodes == null || storeCodes.isEmpty()) return Collections.emptyList();
        String sql = "SELECT * FROM events_master WHERE UPPER(store_code) IN (:codes)";
        Map<String,Object> params = Collections.singletonMap("codes",
                storeCodes.stream().map(String::toUpperCase).collect(Collectors.toList()));
        return jdbc.query(sql, params, eventRowMapper);
    }

    @Override
    public List<Map<String, Object>> getCompletedEventDetails(String storeCode) throws Exception {
        String sql = "SELECT * FROM events_master WHERE store_code = :sc ORDER BY created_at DESC";
        Map<String,Object> params = Collections.singletonMap("sc", storeCode);
        return jdbc.query(sql, params, eventRowMapper);
    }

    @Override
    public List<?> getInvitedMembers(String eventId) throws Exception {
        String sql = "SELECT name, contact, created_at, source FROM event_invitees WHERE event_id = :eid";
        Map<String,Object> params = Collections.singletonMap("eid", eventId);
        return jdbc.queryForList(sql, params);
    }

    @Override
    public List<AttendeesDetailDTO> getAllAttendees(String eventId) throws Exception {
        String sql = "SELECT name, contact, liked, first_time FROM event_attendees WHERE event_id = :eid";
        Map<String,Object> params = Collections.singletonMap("eid", eventId);
        return jdbc.query(sql, params, (rs, i) -> {
            AttendeesDetailDTO d = new AttendeesDetailDTO();
            d.setName(rs.getString("name"));
            d.setPhone(rs.getString("contact"));
            d.setLike(rs.getString("liked"));
            d.setFirstTimeAtTanishq(rs.getBoolean("first_time"));
            return d;
        });
    }

    @Override
    @Transactional
    public QrResponseDTO insertEvent(EventsDetailDTO eventsDetailDTO) throws Exception {
        // Insert into events_master and return QR generation responsibility to service
        String sql = "INSERT INTO events_master (event_id, created_at, store_code, region, event_type, event_sub_type, event_name, rso, start_date, image, invitees, attendees, completed_events_link, community, location, is_attendees_uploaded, sale, advance, ghs_rga, gmb, drive_link, diamond_awareness, ghs_flag, extra_json) " +
                "VALUES (:eventId, :createdAt, :storeCode, :region, :eventType, :eventSubType, :eventName, :rso, :startDate, :image, :invitees, :attendees, :completedEventsLink, :community, :location, :isAttendeesUploaded, :sale, :advance, :ghsRga, :gmb, :driveLink, :diamondAwareness, :ghsFlag, :extraJson)";
        Map<String,Object> params = new HashMap<>();
        params.put("eventId", eventsDetailDTO.getId());
        params.put("createdAt", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("storeCode", eventsDetailDTO.getStoreCode());
        params.put("region", eventsDetailDTO.getRegion());
        params.put("eventType", eventsDetailDTO.getEventType());
        params.put("eventSubType", eventsDetailDTO.getEventSubType());
        params.put("eventName", eventsDetailDTO.getEventName());
        params.put("rso", eventsDetailDTO.getRso());
        params.put("startDate", parseSafeDateToSqlDate(eventsDetailDTO.getStartDate()));
        params.put("image", eventsDetailDTO.getImage());
        params.put("invitees", eventsDetailDTO.getInvitees() == null ? 0 : eventsDetailDTO.getInvitees());
        params.put("attendees", eventsDetailDTO.getAttendees() == null ? 0 : eventsDetailDTO.getAttendees());
        params.put("completedEventsLink", eventsDetailDTO.getCompletedEventsDriveLink());
        params.put("community", eventsDetailDTO.getCommunity());
        params.put("location", eventsDetailDTO.getLocation());
        params.put("isAttendeesUploaded", eventsDetailDTO.isAttendeesUploaded());
        params.put("sale", eventsDetailDTO.getSale() == null ? 0 : eventsDetailDTO.getSale());
        params.put("advance", eventsDetailDTO.getAdvance() == null ? 0 : eventsDetailDTO.getAdvance());
        params.put("ghsRga", eventsDetailDTO.getGhsOrRga() == null ? 0 : eventsDetailDTO.getGhsOrRga());
        params.put("gmb", eventsDetailDTO.getGmb() == null ? 0 : eventsDetailDTO.getGmb());
        params.put("driveLink", eventsDetailDTO.getCompletedEventsDriveLink());
        params.put("diamondAwareness", eventsDetailDTO.isDiamondAwareness());
        params.put("ghsFlag", eventsDetailDTO.isGhsFlag());
        params.put("extraJson", null);

        jdbc.update(sql, params);
        QrResponseDTO qr = new QrResponseDTO();
        qr.setStatus(true);
        qr.setQrData("db-stored"); // service will generate actual QR image as before
        return qr;
    }

    @Override
    @Transactional
    public boolean updateDriveLink(String eventId, String driveLink) throws Exception {
        String sql = "UPDATE events_master SET drive_link = :driveLink WHERE event_id = :eid";
        Map<String,Object> params = Map.of("driveLink", driveLink, "eid", eventId);
        int updated = jdbc.update(sql, params);
        return updated > 0;
    }

    @Override
    public boolean uploadXlsxToSheet(MultipartFile file, String eventId, String sheetId) throws Exception {
        // If you want to keep Sheets sync, call existing google util from service layer.
        throw new UnsupportedOperationException("Use GSheet util for Excel -> sheet uploads. DB stores rows using import runner.");
    }

    @Override
    @Transactional
    public int insertAttendees(AttendeesDetailDTO attendeesDetailDTO) throws Exception {
        // Bulk path: we assume excel path is already parsed by the service/excel util and inserted via import runner
        if (attendeesDetailDTO.getFile() != null && !attendeesDetailDTO.getFile().isEmpty()) {
            // Not implemented here; prefer import runner to do bulk inserts.
            throw new UnsupportedOperationException("Bulk insert from excel is not supported via JdbcEventRepository directly.");
        } else {
            String sql = "INSERT INTO event_attendees (event_id, name, contact, liked, first_time, created_at, is_uploaded_from_excel, rso_name) " +
                    "VALUES (:eventId, :name, :contact, :liked, :firstTime, :createdAt, :isUploadedFromExcel, :rsoName)";
            Map<String,Object> params = new HashMap<>();
            params.put("eventId", attendeesDetailDTO.getId());
            params.put("name", attendeesDetailDTO.getName());
            params.put("contact", attendeesDetailDTO.getPhone());
            params.put("liked", attendeesDetailDTO.getLike());
            params.put("firstTime", attendeesDetailDTO.isFirstTimeAtTanishq());
            params.put("createdAt", new java.sql.Timestamp(System.currentTimeMillis()));
            params.put("isUploadedFromExcel", false);
            params.put("rsoName", attendeesDetailDTO.getRsoName());

            int updated = jdbc.update(sql, params);
            // Return 1 if inserted
            if (updated > 0) {
                // update attendees total in events_master
                updateAttendees(attendeesDetailDTO.getId(), 1);
                return updated;
            }
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean insertInvitee(InviteesDetailDTO inviteesDetailDTO) throws Exception {
        String sql = "INSERT INTO event_invitees (event_id, name, contact, created_at, source) VALUES (:eid, :name, :contact, :createdAt, :source)";
        Map<String,Object> params = new HashMap<>();
        params.put("eid", inviteesDetailDTO.getEventId());
        params.put("name", inviteesDetailDTO.getName());
        params.put("contact", inviteesDetailDTO.getContact());
        params.put("createdAt", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("source", "single");
        int u = jdbc.update(sql, params);
        return u > 0;
    }

    @Override
    @Transactional
    public boolean updateAttendees(String eventId, int newTotal) throws Exception {
        String sql = "UPDATE events_master SET attendees = :total WHERE event_id = :eid";
        Map<String,Object> params = Map.of("total", newTotal, "eid", eventId);
        int updated = jdbc.update(sql, params);
        return updated > 0;
    }

    @Override
    public ResponseDataDTO updateSaleOfAnEvent(String eventCode, String sale) throws Exception {
        String sql = "UPDATE events_master SET sale = :sale WHERE event_id = :eid";
        Map<String,Object> params = Map.of("sale", Double.parseDouble(sale), "eid", eventCode);
        jdbc.update(sql, params);
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(true);
        dto.setMessage("Sale updated");
        return dto;
    }

    @Override
    public ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) throws Exception {
        String sql = "UPDATE events_master SET advance = :advance WHERE event_id = :eid";
        Map<String,Object> params = Map.of("advance", Double.parseDouble(advance), "eid", eventCode);
        jdbc.update(sql, params);
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(true);
        dto.setMessage("Advance updated");
        return dto;
    }

    @Override
    public ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) throws Exception {
        String sql = "UPDATE events_master SET ghs_rga = :val WHERE event_id = :eid";
        Map<String,Object> params = Map.of("val", Double.parseDouble(ghsRga), "eid", eventCode);
        jdbc.update(sql, params);
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(true);
        dto.setMessage("GHS/RGA updated");
        return dto;
    }

    @Override
    public ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) throws Exception {
        String sql = "UPDATE events_master SET gmb = :val WHERE event_id = :eid";
        Map<String,Object> params = Map.of("val", Double.parseDouble(gmb), "eid", eventCode);
        jdbc.update(sql, params);
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(true);
        dto.setMessage("GMB updated");
        return dto;
    }

    @Override
    public void warmEntireEventsCache() throws Exception {
        // no-op: caching belongs to service layer. But a simple select can prime DB caches if desired.
        jdbc.queryForList("SELECT event_id FROM events_master LIMIT 1", Collections.emptyMap());
    }

    @Override
    public void invalidateEventsCache() {
        // no-op here; service-level invalidation remains the same.
    }

    @Override
    public Map<String, Object> getEventById(String eventId) throws Exception {
        String sql = "SELECT * FROM events_master WHERE event_id = :eid";
        Map<String,Object> params = Collections.singletonMap("eid", eventId);
        List<Map<String,Object>> rows = jdbc.query(sql, params, (rs, i) -> {
            Map<String,Object> m = new HashMap<>();
            m.put("Id", rs.getString("event_id"));
            m.put("Attendees", rs.getInt("attendees"));
            m.put("Invitees", rs.getInt("invitees"));
            m.put("StoreCode", rs.getString("store_code"));
            m.put("StartDate", rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate().toString() : "");
            return m;
        });
        return rows.isEmpty() ? null : rows.get(0);
    }
    private java.sql.Date parseSafeDateToSqlDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return java.sql.Date.valueOf(LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"))); }
        catch (Exception e) {
            try { return java.sql.Date.valueOf(LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy/MM/dd"))); }
            catch (Exception ex) { return null; }
        }
    }

}
