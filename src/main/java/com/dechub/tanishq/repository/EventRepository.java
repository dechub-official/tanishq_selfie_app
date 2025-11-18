package com.dechub.tanishq.repository;

import com.dechub.tanishq.dto.ExcelStoreDTO;
import com.dechub.tanishq.dto.eventsDto.*;
import java.util.List;
import java.util.Map;

import com.dechub.tanishq.util.ResponseDataDTO;
import org.springframework.web.multipart.MultipartFile;

public interface EventRepository {

    // read operations
    List<Map<String, Object>> getEventsForStores(List<String> storeCodes) throws Exception;
    List<Map<String, Object>> getCompletedEventDetails(String storeCode) throws Exception;
    List<?> getInvitedMembers(String eventId) throws Exception;
    List<AttendeesDetailDTO> getAllAttendees(String eventId) throws Exception;

    // create/update operations
    QrResponseDTO insertEvent(EventsDetailDTO eventsDetailDTO) throws Exception;
    boolean updateDriveLink(String eventId, String driveLink) throws Exception;
    boolean uploadXlsxToSheet(MultipartFile file, String eventId, String sheetId) throws Exception;

    // granular updates used by service
    int insertAttendees(AttendeesDetailDTO attendeesDetailDTO) throws Exception;
    boolean insertInvitee(InviteesDetailDTO inviteesDetailDTO) throws Exception;


    boolean updateAttendees(String eventId, int addedCount) throws Exception;
    // event numeric updates
    ResponseDataDTO updateSaleOfAnEvent(String eventCode, String sale) throws Exception;
    ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) throws Exception;
    ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) throws Exception;
    ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) throws Exception;

    // cache warm/invalidate
    void warmEntireEventsCache() throws Exception;
    void invalidateEventsCache();

    Map<String, Object> getEventById(String eventId) throws Exception;


}
