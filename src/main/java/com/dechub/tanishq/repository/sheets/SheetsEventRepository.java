package com.dechub.tanishq.repository.sheets;

import com.dechub.tanishq.gsheet.GSheetUserDetailsUtil;
import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.repository.EventRepository;
import com.dechub.tanishq.util.ResponseDataDTO;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class SheetsEventRepository implements EventRepository {

    private final GSheetUserDetailsUtil gSheet;

    public SheetsEventRepository(GSheetUserDetailsUtil gSheet) {
        this.gSheet = gSheet;
    }

    // --- helper to read first available key
    private static String safeGet(Map<String, Object> row, String... keys) {
        if (row == null) return "";
        for (String k : keys) {
            if (k == null) continue;
            if (row.containsKey(k) && row.get(k) != null) {
                String v = row.get(k).toString().trim();
                if (!v.isEmpty()) return v;
            }
            // also try lower-case key to be tolerant
            String lower = k.toLowerCase();
            for (String actual : row.keySet()) {
                if (actual != null && actual.toLowerCase().equals(lower) && row.get(actual) != null) {
                    String v = row.get(actual).toString().trim();
                    if (!v.isEmpty()) return v;
                }
            }
        }
        return "";
    }

    // --- normalize each raw sheet row into canonical keys *and* keep original keys as fallback
    private Map<String, Object> normalizeRow(Map<String, Object> raw) {
        Map<String, Object> out = new HashMap<>();

        // canonical keys your service expects
        out.put("StoreCode",      safeGet(raw, "Store Code", "StoreCode", "Store"));
        out.put("Region",         safeGet(raw, "Region"));
        out.put("Id",             safeGet(raw, "Id", "ID", "EventId", "Event Id"));
        out.put("EventId",        safeGet(raw, "Id", "ID", "EventId", "Event Id")); // keep both Id and EventId
        out.put("EventType",      safeGet(raw, "Event Type", "EventType"));
        out.put("EventSubType",   safeGet(raw, "Event Sub Type", "Event SubType"));
        out.put("EventName",      safeGet(raw, "Event Name", "EventName"));
        out.put("RSO",            safeGet(raw, "RSO"));
        out.put("StartDate",      safeGet(raw, "Start Date", "StartDate"));
        out.put("StartTime",      safeGet(raw, "StartTime", "Time", "start time"));
        out.put("Image",          safeGet(raw, "Image"));
        out.put("Invitees",       safeGet(raw, "Invitees", "invitees"));
        out.put("Attendees",      safeGet(raw, "Attendees", "attendees"));
        out.put("completedEvents", safeGet(raw, "completed Events", "completedEvents", "completed Event", "completedEvents"));
        out.put("Community",      safeGet(raw, "Community"));
        out.put("Location",       safeGet(raw, "location", "Location"));
        out.put("isAttendeesUploaded", safeGet(raw, "isAttendeesUploaded"));
        out.put("sale",           safeGet(raw, "sale", "Sale"));
        out.put("Sale",           safeGet(raw, "sale", "Sale")); // keep both spellings
        out.put("advance",        safeGet(raw, "advance", "Advance"));
        out.put("Advance",        safeGet(raw, "advance", "Advance"));
        out.put("ghs/rga",        safeGet(raw, "ghs/rga", "GhsOrRga", "ghs"));
        out.put("GhsOrRga",       safeGet(raw, "ghs/rga", "GhsOrRga", "ghs"));
        out.put("gmb",            safeGet(raw, "gmb", "Gmb", "GMB"));
        out.put("Gmb",            safeGet(raw, "gmb", "Gmb", "GMB"));
        out.put("Drive link",     safeGet(raw, "Drive link", "DriveLink", "Drive Link"));
        out.put("DriveLink",      safeGet(raw, "Drive link", "DriveLink", "Drive Link"));
        out.put("Diamond Awareness", safeGet(raw, "Diamond Awareness", "DiamondAwareness"));
        out.put("DiamondAwareness", safeGet(raw, "Diamond Awareness", "DiamondAwareness"));
        out.put("GHS",            safeGet(raw, "GHS", "Ghs", "ghs"));

        // preserve original raw fields as fallback for any code that expects original headers
        if (raw != null) {
            raw.forEach((k, v) -> {
                if (!out.containsKey(k)) out.put(k, v);
            });
        }

        return out;
    }

    @Override
    public List<Map<String, Object>> getEventsForStores(List<String> storeCodes) throws Exception {
        List<Map<String, Object>> raw = gSheet.getEventsForStores(storeCodes);
        if (raw == null) return Collections.emptyList();
        return raw.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeRow)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCompletedEventDetails(String storeCode) throws Exception {
        List<Map<String, Object>> raw = gSheet.getCompletedEventDetails(storeCode);
        if (raw == null) return Collections.emptyList();
        return raw.stream().map(this::normalizeRow).collect(Collectors.toList());
    }

    @Override
    public List<?> getInvitedMembers(String eventId) throws Exception {
        return gSheet.getAllAttendees(eventId);
    }

    @Override
    public List<AttendeesDetailDTO> getAllAttendees(String eventId) throws Exception {
        return gSheet.getAllAttendees(eventId);
    }

    @Override
    public QrResponseDTO insertEvent(EventsDetailDTO eventsDetailDTO) throws Exception {
        return gSheet.insertSheetEventsData(eventsDetailDTO);
    }

    @Override
    public boolean updateDriveLink(String eventId, String driveLink) throws Exception {
        return gSheet.updateDrivelink(eventId, driveLink);
    }

    @Override
    public boolean uploadXlsxToSheet(MultipartFile file, String eventId, String sheetId) throws Exception {
        return gSheet.uploadXlsxToGoogleSheet(file, eventId, sheetId);
    }

    @Override
    public int insertAttendees(AttendeesDetailDTO attendeesDetailDTO) throws Exception {
        return gSheet.insertSheetAttendeesData(attendeesDetailDTO);
    }

    @Override
    public boolean insertInvitee(InviteesDetailDTO inviteesDetailDTO) throws Exception {
        return gSheet.insertSheetInviteesData(inviteesDetailDTO);
    }

    @Override
    public ResponseDataDTO updateSaleOfAnEvent(String eventCode, String sale) throws Exception {
        return gSheet.updateSaleOfAnEvent(eventCode, sale);
    }

    @Override
    public ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) throws Exception {
        return gSheet.updateAdvanceOfAnEvent(eventCode, advance);
    }

    @Override
    public ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) throws Exception {
        return gSheet.updateGhsRgaOfAnEvent(eventCode, ghsRga);
    }

    @Override
    public ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) throws Exception {
        return gSheet.updateGmbOfAnEvent(eventCode, gmb);
    }

    @Override
    public boolean updateAttendees(String eventId, int newTotal) throws Exception {
        // delegate to GSheet util - implement update logic in util if not present
        return gSheet.updateAttendees(eventId, newTotal);
    }

    @Override
    public void warmEntireEventsCache() throws Exception {
        gSheet.warmEntireEventsCache();
    }

    @Override
    public void invalidateEventsCache() {
        gSheet.invalidateEventsCache();
    }
    @Override
    public Map<String, Object> getEventById(String eventId) throws Exception {
        if (eventId == null || eventId.trim().isEmpty()) return null;

        // Get all events (cached)
        List<Map<String, Object>> raw = gSheet.getEventsForStores(Collections.emptyList());

        if (raw == null) return null;

        for (Map<String, Object> r : raw) {
            Map<String, Object> normalized = normalizeRow(r);

            String id = safeGet(normalized, "Id", "EventId");

            if (id != null && id.equalsIgnoreCase(eventId)) {
                return normalized;
            }
        }

        return null;
    }

}
