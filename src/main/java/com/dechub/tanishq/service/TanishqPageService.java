package com.dechub.tanishq.service;

import com.dechub.tanishq.dto.*;
import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.entity.*;
import com.dechub.tanishq.entity.Event;
import com.dechub.tanishq.repository.*;
import com.dechub.tanishq.util.CommonConstants;
import com.dechub.tanishq.util.ResponseDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class TanishqPageService {

    @Value("${dechub.bride.upload.dir}")
    private String UPLOAD_DIR;

    @Value("${dechub.base.image}")
    private String BASE_IMG;

    public ArrayList<ExcelStoreDTO> storeList = null;

    @Value("${selfie.upload.dir}")
    private String selfieDirectory;

    @Value("${system.isWindows}")
    private String isWindows;

    @Autowired
    private StoreServices storeServices;

    @Autowired
    private UserSession userSession;

    // JPA Repositories
    @Autowired
    private StoreLoginRepository storeLoginRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private InviteeRepository inviteeRepository;

    @Autowired
    private BrideDetailsRepository brideDetailsRepository;

    @Autowired
    private com.dechub.tanishq.service.excel.ExcelProcessingService excelProcessingService;

    @Autowired
    private RivaahRepository rivaahRepository;

    @Autowired
    private RivaahUserRepository rivaahUserRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private GreetingRepository greetingRepository;

    // In-memory cache for passwords (populated from database)
    private final Map<String, String> passwordCache = new ConcurrentHashMap<>();

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private PasswordChangeHistoryRepository passwordChangeHistoryRepository;

    // RestTemplate for external API calls
    private RestTemplate restTemplate;

    @Value("${book.appoitment.api.username}")
    private String bookAnAppoitmentUsername;
    @Value("${book.appoitment.api.password}")
    private String bookAnAppoitmentPassword;

    @Value("${book.appoitment.api.url}")
    public String bookAnAppoitmentUrl;

    @Value("${book.appointment.api.url}")
    public String bookAnAppointmentUrl;

    private static final Logger log = LoggerFactory.getLogger(TanishqPageService.class);

    @PostConstruct
    public void init() {
        this.restTemplate = restTemplateBuilder.build();
        try {
            // Load all user passwords into cache
            loadPasswordCache();
            log.info("Password cache warmed with {} entries", passwordCache.size());
        } catch (Exception e) {
            log.warn("Could not warm password cache at startup: {}", e.getMessage());
        }
    }

    /**
     * Load all user passwords into memory cache from database
     */
    private void loadPasswordCache() {
        passwordCache.clear();
        List<StoreLogin> users = storeLoginRepository.findAll();
        for (StoreLogin user : users) {
            passwordCache.put(user.getStoreCode().toUpperCase(), user.getPassword());
        }
    }

    /**
     * Handle store login using JPA user authentication
     */
    public EventsLoginResponseDTO eventsLogin(String storeCode, String password) throws Exception {
        EventsLoginResponseDTO response = new EventsLoginResponseDTO();

        String code = storeCode == null ? "" : storeCode.trim();
        String pwd = password == null ? "" : password;

        // Special regional manager codes (these are hardcoded)
        Set<String> regionalManagerCodes = new HashSet<>(Arrays.asList(
                "east1","east2","north1a","north1b","north2","north3","south1","south2a","south3",
                "west1a","west1b","west2","west3","test","north1","west1","south2","north4"
        ));

        // Check password cache first
        String correctPassword = passwordCache.get(code.toUpperCase());

        if (correctPassword == null) {
            // If not in cache, try to find user by username
            Optional<StoreLogin> user = storeLoginRepository.findByStoreCode(code.toUpperCase());
            if (user.isPresent()) {
                correctPassword = user.get().getPassword();
                // Update cache
                passwordCache.put(code.toUpperCase(), correctPassword);
            }
        }

        if (correctPassword == null) {
            response.setStatus(false);
            response.setMessage("User not found.");
            return response;
        }

        if (!pwd.equals(correctPassword)) {
            response.setStatus(false);
            response.setMessage("Invalid credentials.");
            return response;
        }

        // Handle regional manager login
        if (regionalManagerCodes.contains(code.toLowerCase())) {
            Map<String, Object> details = new HashMap<>();
            details.put("manager", code.toUpperCase());
            response.setStoreData(details);
            response.setStatus(true);
            return response;
        }

        // Get store details from database
        Optional<Store> storeOpt = storeRepository.findById(code.toUpperCase());
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            Map<String, Object> details = new HashMap<>();
            details.put("storeCode", store.getStoreCode());
            details.put("storeName", store.getStoreName());
            details.put("storeAddress", store.getStoreAddress());
            details.put("storeCity", store.getStoreCity());
            details.put("storeState", store.getStoreState());
            // Note: Store entity might not have these methods yet - using basic info for now
            response.setStoreData(details);
        } else {
            Map<String, Object> details = new HashMap<>();
            details.put("storeCode", code.toUpperCase());
            response.setStoreData(details);
        }

        response.setStatus(true);
        return response;
    }

    /**
     * Store user details in database (simplified - keeping only basic functionality)
     */
    public ResponseDataDTO storeUserDetails(UserDetailsDTO userDetailsDTO) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();

        try {
            String divisionName = this.getDivisionDirectory(userDetailsDTO.getStoreCode());
            if (divisionName == null) {
                responseDataDTO.setMessage("Invalid Store Code");
                responseDataDTO.setStatus(false);
                return responseDataDTO;
            }

            userDetailsDTO.setDate(this.getCurrentTime());

            // Simplified user details storage - using core DTO method
            UserDetails userDetails = new UserDetails();
            userDetails.setStoreCode(userDetailsDTO.getStoreCode());
            userDetails.setName(userDetailsDTO.getName()); // Use available method
            userDetails.setReason(userDetailsDTO.getReason()); // Use available method
            userDetails.setRsoName(userDetailsDTO.getRsoName()); // Use available method
            userDetails.setDate(LocalDate.now()); // Use current date

            userDetailsRepository.save(userDetails);

            UserDetailResponseDTO userDetailResponseDTO = new UserDetailResponseDTO(true, false);
            responseDataDTO.setMessage(CommonConstants.SUCCESS_CONST);
            responseDataDTO.setResult(userDetailResponseDTO);
            responseDataDTO.setStatus(true);

        } catch (Exception e) {
            responseDataDTO.setMessage("Error saving user details: " + e.getMessage());
            responseDataDTO.setStatus(false);
        }

        return responseDataDTO;
    }

    /**
     * Store event details in database
     */
    @Transactional
    public QrResponseDTO storeEventsDetails(EventsDetailDTO eventsDetailDTO) {
        log.info("=== START storeEventsDetails === Store: {}, EventName: {}, HasFile: {}", 
                eventsDetailDTO.getStoreCode(), eventsDetailDTO.getEventName(), 
                eventsDetailDTO.getFile() != null && !eventsDetailDTO.getFile().isEmpty());
        
        QrResponseDTO response = new QrResponseDTO();
        response.setStatus(false);

        try {
            // Generate event ID
            String eventId = eventsDetailDTO.getStoreCode() + "_" + UUID.randomUUID().toString();
            eventsDetailDTO.setId(eventId);
            log.info("Generated eventId: {}", eventId);

            // Find store
            Optional<Store> storeOpt = storeRepository.findById(eventsDetailDTO.getStoreCode().toUpperCase());
            if (!storeOpt.isPresent()) {
                response.setQrData("Store not found: " + eventsDetailDTO.getStoreCode());
                return response;
            }
            Store store = storeOpt.get();

            // Create and save event
            Event event = new Event();
            event.setId(eventId);
            event.setStore(store);
            event.setEventType(eventsDetailDTO.getEventType());
            event.setEventSubType(eventsDetailDTO.getEventSubType());
            event.setEventName(eventsDetailDTO.getEventName());
            event.setRso(eventsDetailDTO.getRso());
            event.setStartDate(eventsDetailDTO.getStartDate());
            event.setImage(eventsDetailDTO.getImage());
            Integer invitees = eventsDetailDTO.getInvitees();
            event.setInvitees(invitees != null ? invitees : 0);
            event.setAttendees(0); // start with 0
            event.setCompletedEventsDriveLink(eventsDetailDTO.getCompletedEventsDriveLink());
            event.setCommunity(eventsDetailDTO.getCommunity());
            event.setLocation(eventsDetailDTO.getLocation());
            event.setAttendeesUploaded(false);
            event.setSale(eventsDetailDTO.getSale() != null ? eventsDetailDTO.getSale().doubleValue() : 0.0);
            event.setAdvance(eventsDetailDTO.getAdvance() != null ? eventsDetailDTO.getAdvance().doubleValue() : 0.0);
            event.setGhsOrRga(eventsDetailDTO.getGhsOrRga() != null ? eventsDetailDTO.getGhsOrRga().doubleValue() : 0.0);
            event.setGmb(eventsDetailDTO.getGmb() != null ? eventsDetailDTO.getGmb().doubleValue() : 0.0);
            event.setDiamondAwareness(eventsDetailDTO.isDiamondAwareness());
            event.setGhsFlag(eventsDetailDTO.isGhsFlag());
            event.setCreatedAt(LocalDateTime.now());

            eventRepository.save(event);

            // Process Excel file for invitees if provided
            if (eventsDetailDTO.getFile() != null && !eventsDetailDTO.getFile().isEmpty()) {
                try {
                    log.info("Processing Excel file for invitees, file size: {} bytes", eventsDetailDTO.getFile().getSize());
                    List<List<Object>> excelData = excelProcessingService.readExcelFile(eventsDetailDTO.getFile());
                    log.info("Read {} rows from Excel file", excelData.size());
                    int inviteeCount = 0;
                    
                    for (List<Object> row : excelData) {
                        if (row.size() >= 2) {
                            String name = excelProcessingService.safeString(row, 0);
                            String contact = excelProcessingService.safeString(row, 1);
                            
                            if (!name.isEmpty() || !contact.isEmpty()) {
                                Invitee invitee = new Invitee();
                                invitee.setEvent(event);
                                invitee.setName(name);
                                invitee.setContact(contact);
                                invitee.setCreatedAt(LocalDateTime.now());
                                inviteeRepository.save(invitee);
                                inviteeCount++;
                                log.debug("Saved invitee: {} - {}", name, contact);
                            }
                        }
                    }
                    
                    // Update event invitee count
                    event.setInvitees(inviteeCount);
                    eventRepository.save(event);
                    log.info("Successfully saved {} invitees for event {}", inviteeCount, eventId);
                } catch (Exception e) {
                    log.error("Failed to process invitees from Excel for event " + eventId, e);
                }
            }

            // Generate QR code (simplified - in real implementation would use QrCodeService)
            String qrCode = generateSimpleQrCode(eventId);
            if (qrCode == null || qrCode.equals("error")) {
                response.setQrData("Error generating QR code");
                return response;
            }

            response.setStatus(true);
            response.setQrData("data:image/png;base64," + qrCode);
            log.info("=== SUCCESS storeEventsDetails === EventId: {}", eventId);

        } catch (Exception e) {
            log.error("=== FAILED storeEventsDetails === Error: " + e.getMessage(), e);
            response.setQrData("Error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Simple QR code generation (placeholder - would need ZXing library implementation)
     */
    private String generateSimpleQrCode(String eventId) {
        try {
            // In real implementation, use ZXing to generate QR code
            // For now, return success
            return "QR_CODE_PLACEHOLDER_" + eventId;
        } catch (Exception e) {
            return "error";
        }
    }

    /**
     * Store attendees data in database and update event attendee count
     */
    @Transactional
    public ResponseDataDTO storeAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
        ResponseDataDTO dto = new ResponseDataDTO();

        try {
            Optional<Event> eventOpt = eventRepository.findById(attendeesDetailDTO.getId());
            if (!eventOpt.isPresent()) {
                dto.setStatus(false);
                dto.setMessage("Event not found");
                return dto;
            }
            Event event = eventOpt.get();

            // Check if this is a bulk upload
            if (attendeesDetailDTO.isBulkUpload() && attendeesDetailDTO.getFile() != null && !attendeesDetailDTO.getFile().isEmpty()) {
                // Process Excel file for bulk attendees
                try {
                    log.info("Processing Excel file for bulk attendees, file size: {} bytes", attendeesDetailDTO.getFile().getSize());
                    List<List<Object>> excelData = excelProcessingService.readExcelFile(attendeesDetailDTO.getFile());
                    List<List<Object>> processedData = excelProcessingService.addMetadataAndEventId(excelData, attendeesDetailDTO.getId(), true);

                    int attendeeCount = 0;
                    for (List<Object> row : processedData) {
                        // Expected row structure after processing: [eventId, name, phone, like, firstTime, timestamp, isUploadedFromExcel, rsoName]
                        String name = excelProcessingService.safeString(row, 1);
                        String phone = excelProcessingService.safeString(row, 2);
                        String like = excelProcessingService.safeString(row, 3);
                        boolean firstTime = Boolean.parseBoolean(excelProcessingService.safeString(row, 4));
                        String rsoName = excelProcessingService.safeString(row, 7); // RSO name at index 7

                        // Create attendee record
                        Attendee attendee = new Attendee();
                        attendee.setEvent(event);
                        attendee.setName(name);
                        attendee.setPhone(phone);
                        attendee.setLike(like);
                        attendee.setFirstTimeAtTanishq(firstTime);
                        attendee.setCreatedAt(LocalDateTime.now());
                        attendee.setIsUploadedFromExcel(true);
                        attendee.setRsoName(rsoName); // Set RSO name

                        attendeeRepository.save(attendee);
                        attendeeCount++;
                        log.debug("Saved attendee: {} - {} - {}", name, phone, rsoName);
                    }

                    // Update event attendee count
                    event.setAttendees(attendeeCount);
                    event.setAttendeesUploaded(true);
                    eventRepository.save(event);

                    dto.setStatus(true);
                    dto.setMessage(attendeeCount + " attendees uploaded successfully");
                    dto.setResult(attendeeCount);

                } catch (Exception e) {
                    log.error("Failed to process bulk attendees from Excel for event " + attendeesDetailDTO.getId(), e);
                    dto.setStatus(false);
                    dto.setMessage("Error processing Excel file: " + e.getMessage());
                }
            } else {
                // Single attendee upload (existing logic)
                Attendee attendee = new Attendee();
                attendee.setEvent(event);
                attendee.setName(attendeesDetailDTO.getName());
                attendee.setPhone(attendeesDetailDTO.getPhone());
                attendee.setLike(attendeesDetailDTO.getLike());
                attendee.setFirstTimeAtTanishq(attendeesDetailDTO.isFirstTimeAtTanishq());
                attendee.setCreatedAt(LocalDateTime.now());
                attendee.setIsUploadedFromExcel(false);
                attendee.setRsoName(attendeesDetailDTO.getRsoName());

                attendeeRepository.save(attendee);

                // Update event attendee count
                event.setAttendees(event.getAttendees() + 1);
                eventRepository.save(event);

                dto.setStatus(true);
                dto.setMessage("Attendee stored successfully");
                dto.setResult(1); // Number inserted
            }

        } catch (IllegalArgumentException iae) {
            dto.setStatus(false);
            dto.setMessage(iae.getMessage());
        } catch (Exception e) {
            dto.setStatus(false);
            dto.setMessage("Error: " + e.getMessage());
        }

        return dto;
    }

    /**
     * Get completed events for a store/region using JPA
     */
    public CompletedEventsResponseDTO getAllCompletedEvents(String code) {
        CompletedEventsResponseDTO out = new CompletedEventsResponseDTO();

        try {
            if (code == null || code.trim().isEmpty()) {
                out.setStatus(false);
                out.setMessage("Invalid code");
                return out;
            }

            List<Map<String, Object>> events = new ArrayList<>();

            // Regional manager codes
            Set<String> regions = new HashSet<>(Arrays.asList(
                    "North1","North2","North3","North4",
                    "South1","South2","South3",
                    "East1","East2",
                    "West1","West2","West3"
            ));

            List<String> storeCodes;
            if (regions.contains(code.trim())) {
                // Region: get all store codes for that region
                storeCodes = getStoreCodesByRegion(code.trim());
            } else {
                // Single store
                storeCodes = Collections.singletonList(code.trim());
            }

            // Get events for stores
            List<Event> eventEntities = eventRepository.findByStoreCodeIn(storeCodes);

            // Convert to Map format (to maintain API compatibility)
            for (Event event : eventEntities) {
                // Show all events, not just those with drive links
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("StoreCode", event.getStore().getStoreCode());
                eventMap.put("Id", event.getId());
                eventMap.put("EventType", event.getEventType());
                eventMap.put("EventSubType", event.getEventSubType());
                eventMap.put("EventName", event.getEventName());
                eventMap.put("RSO", event.getRso());
                eventMap.put("StartDate", event.getStartDate());
                eventMap.put("StartTime", ""); // Not in entity
                eventMap.put("Description", ""); // Not in entity
                eventMap.put("Image", event.getImage());
                eventMap.put("Invitees", event.getInvitees());
                eventMap.put("Attendees", event.getAttendees());
                eventMap.put("createdAt", event.getCreatedAt());
                eventMap.put("completedEvent", event.getCompletedEventsDriveLink() != null && !event.getCompletedEventsDriveLink().trim().isEmpty());
                eventMap.put("Community", event.getCommunity());
                eventMap.put("location", event.getLocation());
                // Count actual uploaded attendees from attendees table
                long uploadedAttendeesCount = attendeeRepository.countByEventId(event.getId());
                eventMap.put("isAttendeesUploaded", uploadedAttendeesCount);
                eventMap.put("sale", event.getSale());
                eventMap.put("advance", event.getAdvance());
                eventMap.put("ghs/rga", event.getGhsOrRga());
                eventMap.put("gmb", event.getGmb());
                eventMap.put("completedEvents", event.getCompletedEventsDriveLink() != null ? event.getCompletedEventsDriveLink() : "");

                events.add(eventMap);
            }

            if (events.isEmpty()) {
                out.setStatus(false);
                out.setMessage("No completed events found");
                out.setEventData(Collections.emptyList());
            } else {
                out.setStatus(true);
                out.setMessage("Events fetched successfully");
                out.setEventData(events);
            }

        } catch (Exception e) {
            out.setStatus(false);
            out.setMessage("Error: " + e.getMessage());
            out.setEventData(Collections.emptyList());
        }

        return out;
    }

    /**
     * Get store codes by region using JPA relationships
     */
    private List<String> getStoreCodesByRegion(String region) {
        // This would need region mapping - for now return empty
        // Ideally, stores would have a region field or separate Region entity
        return new ArrayList<>();
    }

    /**
     * Get invitees for an event from database
     */
    public List<?> getInvitedMember(String eventId) {
        try {
            List<Invitee> invitees = inviteeRepository.findByEventId(eventId);
            return invitees.stream().map(invitee -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", invitee.getName());
                map.put("contact", invitee.getContact());
                return map;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Authenticate ABM user from database
     */
    public Optional<LoginResponseDTO> authenticateAbm(String username, String password) {
        try {
            Optional<StoreLogin> user = storeLoginRepository.findByStoreCodeAndPassword(username, password);
            if (user.isPresent() && "ABM".equals(user.get().getRole())) {
                LoginResponseDTO dto = new LoginResponseDTO(user.get().getStoreCode(), user.get().getName());
                return Optional.of(dto);
            }
        } catch (Exception e) {
            log.error("ABM authentication error", e);
        }
        return Optional.empty();
    }

    /**
     * Authenticate RBM user from database
     */
    public Optional<LoginResponseDTO> authenticateRbm(String username, String password) {
        try {
            Optional<StoreLogin> user = storeLoginRepository.findByStoreCodeAndPassword(username, password);
            if (user.isPresent() && "RBM".equals(user.get().getRole())) {
                LoginResponseDTO dto = new LoginResponseDTO(user.get().getStoreCode(), user.get().getName());
                return Optional.of(dto);
            }
        } catch (Exception e) {
            log.error("RBM authentication error", e);
        }
        return Optional.empty();
    }

    /**
     * Authenticate CEE user from database
     */
    public Optional<LoginResponseDTO> authenticateCee(String username, String password) {
        try {
            Optional<StoreLogin> user = storeLoginRepository.findByStoreCodeAndPassword(username, password);
            if (user.isPresent() && "CEE".equals(user.get().getRole())) {
                LoginResponseDTO dto = new LoginResponseDTO(user.get().getStoreCode(), user.get().getName());
                return Optional.of(dto);
            }
        } catch (Exception e) {
            log.error("CEE authentication error", e);
        }
        return Optional.empty();
    }

    /**
     * Get stores by RBM username
     */
    public List<String> fetchStoresByRbm(String rbmUsername) throws Exception {
        return storeRepository.findByRbmUsername(rbmUsername)
                .stream()
                .map(Store::getStoreCode)
                .collect(Collectors.toList());
    }

    /**
     * Get stores by ABM username
     */
    public List<String> fetchStoresByAbm(String abmUsername) throws Exception {
        return storeRepository.findByAbmUsername(abmUsername)
                .stream()
                .map(Store::getStoreCode)
                .collect(Collectors.toList());
    }

    /**
     * Get stores by CEE username
     */
    public List<String> fetchStoresByCee(String ceeUsername) throws Exception {
        return storeRepository.findByCeeUsername(ceeUsername)
                .stream()
                .map(Store::getStoreCode)
                .collect(Collectors.toList());
    }

    /**
     * Get all event data for stores (for dashboard)
     */
    public List<Map<String, Object>> getOnlyEventsForStores(List<String> storeCodes) {
        try {
            List<Event> events = eventRepository.findByStoreCodeIn(storeCodes);
            return events.stream().map(this::convertEventToMap).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Convert Event entity to Map (for API compatibility)
     */
    private Map<String, Object> convertEventToMap(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", event.getId());
        map.put("advance", event.getAdvance());
        map.put("attendees", event.getAttendees());
        map.put("attendees_uploaded", event.getAttendeesUploaded());
        map.put("community", event.getCommunity());
        map.put("completed_events_drive_link", event.getCompletedEventsDriveLink());
        map.put("created_at", event.getCreatedAt());
        map.put("diamond_awareness", event.getDiamondAwareness());
        map.put("event_name", event.getEventName());
        map.put("event_sub_type", event.getEventSubType());
        map.put("event_type", event.getEventType());
        map.put("ghs_flag", event.getGhsFlag());
        map.put("ghs_or_rga", event.getGhsOrRga());
        map.put("gmb", event.getGmb());
        map.put("image", event.getImage());
        map.put("invitees", event.getInvitees());
        map.put("location", event.getLocation());
        map.put("region", event.getRegion());
        map.put("rso", event.getRso());
        map.put("sale", event.getSale());
        map.put("start_date", event.getStartDate());
        map.put("store_code", event.getStore().getStoreCode());
        return map;
    }

    // Utility methods (unchanged from original)
    public boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) return false;
        int len = searchStr.length();
        int max = str.length() - len;
        for (int i = 0; i <= max; i++) {
            if (str.regionMatches(true, i, searchStr, 0, len)) return true;
        }
        return false;
    }

    public ResponseDataDTO saveImage(MultipartFile file, String storeCode) {
        ResponseDataDTO dto = new ResponseDataDTO();

        String divisionName = this.getDivisionDirectory(storeCode);
        if (divisionName == null) {
            dto.setMessage("Invalid Store Code");
            return dto;
        }

        try {
            String newFileName = this.getNewFileName(file.getOriginalFilename());
            String sep = isWindows.equalsIgnoreCase("Y") ? "\\" : "/";
            String filePath = selfieDirectory + sep + divisionName + sep + newFileName;

            File targetFile = new File(filePath);
            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create directory: " + parent);
            }

            file.transferTo(targetFile);
            dto.setMessage(CommonConstants.SUCCESS_CONST);
            dto.setResult(newFileName);
        } catch (Exception e) {
            dto.setMessage(e.getMessage());
        }
        return dto;
    }

    public String getNewFileName(String fileName) {
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                return getCurrentTimeAsUnique();
            }
            String[] lst = fileName.split("\\.");
            String extension = (lst.length > 1) ? lst[lst.length - 1] : "";
            return extension.isEmpty() ? getCurrentTimeAsUnique()
                    : getCurrentTimeAsUnique() + "." + extension;
        } catch (Exception e) {
            log.info(e.getMessage() + " with file name: " + fileName);
            return getCurrentTimeAsUnique();
        }
    }

    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return dtf.format(LocalDateTime.now());
    }

    public String getCurrentTimeAsUnique() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return dtf.format(LocalDateTime.now());
    }

    public String getDivisionDirectory(String storeCode) {
        String result = null;
        Map<String, List<String>> storeDetails = storeServices.getStoreDetails();
        for (Map.Entry<String, List<String>> entry : storeDetails.entrySet()) {
            boolean present = entry.getValue().stream().anyMatch(i -> i.equalsIgnoreCase(storeCode));
            if (present) { result = entry.getKey(); break; }
        }
        return result;
    }

    // Stub methods for missing functionality (return basic responses)
    public List<storeCodeDataDTO> getStoresByRegion(String region) {
        // Stub - return empty list for now
        return new ArrayList<>();
    }

    public ResponseDataDTO updateSaleOfAnEvent(String eventCode, String sale) {
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(false);
        dto.setMessage("Function disabled - use JPA entities directly");
        return dto;
    }

    public ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) {
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(false);
        dto.setMessage("Function disabled - use JPA entities directly");
        return dto;
    }

    public ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) {
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(false);
        dto.setMessage("Function disabled - use JPA entities directly");
        return dto;
    }

    public ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) {
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(false);
        dto.setMessage("Function disabled - use JPA entities directly");
        return dto;
    }

    @Transactional
    public ResponseDataDTO changePasswordForEventManager(String storeCode, String oldPassword, String newPassword) {
        ResponseDataDTO dto = new ResponseDataDTO();

        // 1) Basic validation
        if (storeCode == null || storeCode.trim().isEmpty()) {
            dto.setStatus(false);
            dto.setMessage("Store code is required");
            return dto;
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            dto.setStatus(false);
            dto.setMessage("Old password is required");
            return dto;
        }
        if (newPassword == null || newPassword.isEmpty()) {
            dto.setStatus(false);
            dto.setMessage("New password is required");
            return dto;
        }

        String code = storeCode.trim().toUpperCase();

        // 2) Find user by storeCode
        Optional<StoreLogin> optUser = storeLoginRepository.findByStoreCode(code);
        if (optUser.isEmpty()) {
            dto.setStatus(false);
            dto.setMessage("User not found for store code: " + code);
            return dto;
        }

        StoreLogin user = optUser.get();

        // 3) Check old password
        if (!oldPassword.equals(user.getPassword())) {
            dto.setStatus(false);
            dto.setMessage("Old password is incorrect");
            return dto;
        }

        // 4) Update password in DB
        user.setPassword(newPassword);
        storeLoginRepository.save(user);

        // 5) Write history record
        PasswordChangeHistory history = new PasswordChangeHistory();
        history.setStoreCode(code);
        history.setOldPassword(oldPassword);
        history.setNewPassword(newPassword);
        history.setChangedAt(LocalDateTime.now());
        passwordChangeHistoryRepository.save(history);

        // 6) Update in-memory cache so login uses new password immediately
        passwordCache.put(code, newPassword);

        dto.setStatus(true);
        dto.setMessage("Password changed successfully");
        return dto;
    }


    public StoreSummaryWrapperDTO fetchStoreSummariesByRbmParallel(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        // Stub - return empty summary
        return new StoreSummaryWrapperDTO(new ArrayList<>(), null);
    }

    public StoreSummaryWrapperDTO fetchStoreSummariesByAbmParallel(String abmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        // Stub - return empty summary
        return new StoreSummaryWrapperDTO(new ArrayList<>(), null);
    }

    public StoreSummaryWrapperDTO fetchStoreSummariesByCeeParallel(String ceeUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        // Stub - return empty summary
        return new StoreSummaryWrapperDTO(new ArrayList<>(), null);
    }

    public List<Map<String, Object>> filterEventsByStartDate(List<Map<String, Object>> events, String startDateStr, String endDateStr) {
        // Stub - return all events unfiltered
        return events;
    }

    public ResponseDataDTO getStoreCode() {
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setMessage(CommonConstants.SUCCESS_CONST);
        Map<String, List<String>> storesData = storeServices.getStoreDetails();
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : storesData.entrySet()) {
            result.addAll(entry.getValue());
        }
        dto.setResult(result);
        return dto;
    }

    public StoreEventSummaryDTO processSingleStoreCode(String storeCode, LocalDate startDate, LocalDate endDate) throws Exception {
        List<Event> events = eventRepository.findByStoreCode(storeCode);

        // Filter by date range
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : events) {
            if (startDate != null && endDate != null) {
                try {
                    LocalDate eventDate = LocalDate.parse(event.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
                        filteredEvents.add(event);
                    }
                } catch (Exception e) {
                    // Skip date parsing errors
                }
            } else {
                filteredEvents.add(event);
            }
        }

        double totalAdvance = 0, totalGhsOrRga = 0, totalSale = 0;
        int totalInvitees = 0, totalAttendees = 0;

        for (Event event : filteredEvents) {
            totalInvitees += event.getInvitees() != null ? event.getInvitees() : 0;
            totalAttendees += event.getAttendees() != null ? event.getAttendees() : 0;
            totalAdvance += event.getAdvance() != null ? event.getAdvance() : 0;
            totalGhsOrRga += event.getGhsOrRga() != null ? event.getGhsOrRga() : 0;
            totalSale += event.getSale() != null ? event.getSale() : 0;
        }

        return new StoreEventSummaryDTO(
                storeCode,
                filteredEvents.size(),
                totalInvitees,
                totalAttendees,
                totalAdvance,
                totalGhsOrRga,
                totalSale
        );
    }

    /**
     * Store bride details in database
     */
    public ResponseEntity<byte[]> storeBrideDetails(String brideType, String brideEvent, String brideName, String phone, String date, String email, String zipCode, String filepath) {
        try {
            BrideDetails brideDetails = new BrideDetails();
            brideDetails.setBrideType(brideType);
            brideDetails.setBrideEvent(brideEvent);
            brideDetails.setBrideName(brideName);
            brideDetails.setPhone(phone);
            // Parse date string to LocalDate
            if (date != null && !date.trim().isEmpty()) {
                try {
                    brideDetails.setDate(LocalDate.parse(date));
                } catch (Exception e) {
                    log.warn("Could not parse date: {}, setting to null", date);
                    brideDetails.setDate(null);
                }
            }
            brideDetails.setEmail(email);
            if (zipCode != null && !zipCode.trim().isEmpty()) {
                brideDetails.setZipCode(zipCode);
            }

            brideDetailsRepository.save(brideDetails);

            log.info("Bride details saved successfully for: {}", brideName);

            // Return a simple success image or file (you can implement image generation logic)
            // For now, return empty response but in success status
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error saving bride details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // Keep existing methods that work (image processing, etc.)
    // ... (keeping existing helper methods)
}
