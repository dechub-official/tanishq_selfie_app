package com.dechub.tanishq.service;

import com.dechub.tanishq.dto.*;
import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.dto.rivaahDto.BookAppointmentDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahAllDetailsDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahDTO;
import com.dechub.tanishq.dto.rivaahDto.RivaahImagesDTO;
import com.dechub.tanishq.entity.AbmLogin;
import com.dechub.tanishq.entity.Attendee;
import com.dechub.tanishq.entity.BrideDetails;
import com.dechub.tanishq.entity.CeeLogin;
import com.dechub.tanishq.entity.Event;
import com.dechub.tanishq.entity.Greeting;
import com.dechub.tanishq.entity.Invitee;
import com.dechub.tanishq.entity.PasswordHistory;
import com.dechub.tanishq.entity.ProductDetail;
import com.dechub.tanishq.entity.RbmLogin;
import com.dechub.tanishq.entity.Rivaah;
import com.dechub.tanishq.entity.RivaahUser;
import com.dechub.tanishq.entity.Store;
import com.dechub.tanishq.entity.User;
import com.dechub.tanishq.entity.UserDetails;
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
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
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
    private UserRepository userRepository;

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
    private RbmLoginRepository rbmLoginRepository;

    @Autowired
    private CeeLoginRepository ceeLoginRepository;

    @Autowired
    private AbmLoginRepository abmLoginRepository;

    @Autowired
    private com.dechub.tanishq.service.excel.ExcelProcessingService excelProcessingService;

    @Autowired
    private com.dechub.tanishq.service.events.EventQrCodeService eventQrCodeService;

    @Autowired
    private RivaahRepository rivaahRepository;

    @Autowired
    private RivaahUserRepository rivaahUserRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private GreetingRepository greetingRepository;

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

    // In-memory cache for passwords (populated from database)
    private final Map<String, String> passwordCache = new ConcurrentHashMap<>();

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

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
        
        // Load store users
        List<User> users = userRepository.findAll();
        for (User user : users) {
            passwordCache.put(user.getUsername().toUpperCase(), user.getPassword());
        }
        
        // Load RBM logins
        List<RbmLogin> rbmLogins = rbmLoginRepository.findAll();
        for (RbmLogin rbm : rbmLogins) {
            passwordCache.put(rbm.getRbmUserId().toUpperCase(), rbm.getPassword());
        }
        
        // Load CEE logins
        List<CeeLogin> ceeLogins = ceeLoginRepository.findAll();
        for (CeeLogin cee : ceeLogins) {
            passwordCache.put(cee.getCeeUserId().toUpperCase(), cee.getPassword());
        }
        
        // Load ABM logins
        List<AbmLogin> abmLogins = abmLoginRepository.findAll();
        for (AbmLogin abm : abmLogins) {
            passwordCache.put(abm.getAbmUserId().toUpperCase(), abm.getPassword());
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
                "west1a","west1b","west2","west3","north1","west1","south2","north4"
        ));

        // Check password cache first
        String correctPassword = passwordCache.get(code.toUpperCase());

        if (correctPassword == null) {
            // If not in cache, try to find user by username
            Optional<User> user = userRepository.findByUsername(code.toUpperCase());
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
            // Frontend expects these exact field names
            details.put("BtqCode", store.getStoreCode());
            details.put("BtqName", store.getStoreName());
            details.put("BtqEmailid", store.getStoreEmailId());
            // Additional fields for potential use
            details.put("storeAddress", store.getStoreAddress());
            details.put("storeCity", store.getStoreCity());
            details.put("storeState", store.getStoreState());
            response.setStoreData(details);
        } else {
            Map<String, Object> details = new HashMap<>();
            details.put("BtqCode", code.toUpperCase());
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

            // Generate QR code using dedicated Event QR Code Service
            // This service is separate from Greeting QR functionality
            String qrCode;
            try {
                qrCode = eventQrCodeService.generateEventQrCode(eventId);
            } catch (Exception qrError) {
                log.error("Failed to generate QR code for event {}: {}", eventId, qrError.getMessage());
                response.setQrData("Error generating QR code: " + qrError.getMessage());
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
     * Store attendees data in database and update event attendee count
     */
    @Transactional
    public ResponseDataDTO storeAttendeesData(AttendeesDetailDTO attendeesDetailDTO) {
        ResponseDataDTO dto = new ResponseDataDTO();

        try {
            log.info("=== START storeAttendeesData === EventId: {}, Name: {}, Phone: {}",
                    attendeesDetailDTO.getId(), attendeesDetailDTO.getName(), attendeesDetailDTO.getPhone());

            Optional<Event> eventOpt = eventRepository.findById(attendeesDetailDTO.getId());
            if (!eventOpt.isPresent()) {
                log.error("Event not found with ID: {}", attendeesDetailDTO.getId());
                dto.setStatus(false);
                dto.setMessage("Event not found");
                return dto;
            }
            Event event = eventOpt.get();
            log.info("Found event: {} - Current attendee count: {}", event.getId(), event.getAttendees());

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
                log.info("Processing single attendee: {} - {}", attendeesDetailDTO.getName(), attendeesDetailDTO.getPhone());

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
                log.info("Saved attendee with ID: {}", attendee.getId());

                // Update event attendee count - FIXED: Handle null case
                Integer currentCount = event.getAttendees();
                Integer newCount = currentCount != null ? currentCount + 1 : 1;
                event.setAttendees(newCount);
                eventRepository.save(event);
                log.info("Updated event {} attendee count from {} to {}", event.getId(), currentCount, newCount);

                dto.setStatus(true);
                dto.setMessage("Attendee stored successfully");
                dto.setResult(1); // Number inserted
                log.info("=== SUCCESS storeAttendeesData === EventId: {}, New Count: {}", event.getId(), newCount);
            }

        } catch (IllegalArgumentException iae) {
            log.error("Validation error in storeAttendeesData: {}", iae.getMessage(), iae);
            dto.setStatus(false);
            dto.setMessage(iae.getMessage());
        } catch (Exception e) {
            log.error("=== FAILED storeAttendeesData === EventId: {}, Error: {}",
                    attendeesDetailDTO.getId(), e.getMessage(), e);
            dto.setStatus(false);
            dto.setMessage("Error: " + e.getMessage());
        }

        return dto;
    }

    /**
     * Get completed events for a store/region using JPA
     */
    public CompletedEventsResponseDTO getAllCompletedEvents(String code, String startDateStr, String endDateStr) {
        CompletedEventsResponseDTO out = new CompletedEventsResponseDTO();

        try {
            if (code == null || code.trim().isEmpty()) {
                out.setStatus(false);
                out.setMessage("Invalid code");
                return out;
            }

            List<Map<String, Object>> events = new ArrayList<>();
            List<String> storeCodes;

            // Check if it's a manager username (RBM, CEE, or ABM)
            if (code.contains("-CEE-")) {
                // CEE username (e.g., "EAST1-CEE-01")
                storeCodes = fetchStoresByCee(code.trim());
            } else if (code.contains("-ABM-")) {
                // ABM username (e.g., "EAST1-ABM-01")
                storeCodes = fetchStoresByAbm(code.trim());
            } else if (code.matches("^(EAST|WEST|NORTH|SOUTH)\\d+$")) {
                // RBM username (e.g., "EAST1", "NORTH2", "SOUTH3", "WEST1")
                storeCodes = fetchStoresByRbm(code.trim());
            } else {
                // Single store code
                storeCodes = Collections.singletonList(code.trim());
            }

            // Parse date filters
            LocalDate startDate = null;
            LocalDate endDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                try {
                    startDate = LocalDate.parse(startDateStr.trim(), formatter);
                } catch (Exception e) {
                    // Ignore parsing error, proceed without filter
                }
            }
            
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                try {
                    endDate = LocalDate.parse(endDateStr.trim(), formatter);
                } catch (Exception e) {
                    // Ignore parsing error, proceed without filter
                }
            }

            // Get events for stores
            List<Event> eventEntities = eventRepository.findByStoreCodeIn(storeCodes);

            // Filter events by date range
            List<Event> filteredEventEntities = new ArrayList<>();
            for (Event event : eventEntities) {
                if (startDate != null && endDate != null) {
                    try {
                        LocalDate eventDate = LocalDate.parse(event.getStartDate(), formatter);
                        if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
                            filteredEventEntities.add(event);
                        }
                    } catch (Exception e) {
                        // Skip events with invalid dates
                    }
                } else {
                    filteredEventEntities.add(event);
                }
            }

            // Convert to Map format (to maintain API compatibility)
            for (Event event : filteredEventEntities) {
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
            Optional<AbmLogin> abmLogin = abmLoginRepository.findByAbmUserIdAndPassword(username, password);
            if (abmLogin.isPresent()) {
                LoginResponseDTO dto = new LoginResponseDTO(abmLogin.get().getAbmUserId(), abmLogin.get().getAbmName());
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
            Optional<RbmLogin> rbmLogin = rbmLoginRepository.findByRbmUserIdAndPassword(username, password);
            if (rbmLogin.isPresent()) {
                LoginResponseDTO dto = new LoginResponseDTO(rbmLogin.get().getRbmUserId(), rbmLogin.get().getRbmName());
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
            Optional<CeeLogin> ceeLogin = ceeLoginRepository.findByCeeUserIdAndPassword(username, password);
            if (ceeLogin.isPresent()) {
                LoginResponseDTO dto = new LoginResponseDTO(ceeLogin.get().getCeeUserId(), ceeLogin.get().getCeeName());
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
        map.put("advance", event.getAdvance() != null ? event.getAdvance() : 0);
        map.put("attendees", event.getAttendees() != null ? event.getAttendees() : 0);
        map.put("attendees_uploaded", event.getAttendeesUploaded() != null ? event.getAttendeesUploaded() : false);
        map.put("community", event.getCommunity() != null ? event.getCommunity() : "");
        map.put("completed_events_drive_link", event.getCompletedEventsDriveLink() != null ? event.getCompletedEventsDriveLink() : "");
        map.put("created_at", event.getCreatedAt());
        map.put("diamond_awareness", event.getDiamondAwareness() != null ? event.getDiamondAwareness() : false);
        map.put("event_name", event.getEventName() != null ? event.getEventName() : "");
        map.put("event_sub_type", event.getEventSubType() != null ? event.getEventSubType() : "");
        map.put("event_type", event.getEventType() != null ? event.getEventType() : "");
        map.put("ghs_flag", event.getGhsFlag() != null ? event.getGhsFlag() : false);
        map.put("ghs_or_rga", event.getGhsOrRga() != null ? event.getGhsOrRga() : 0);
        map.put("gmb", event.getGmb() != null ? event.getGmb() : 0);
        map.put("image", event.getImage() != null ? event.getImage() : "");
        map.put("invitees", event.getInvitees() != null ? event.getInvitees() : 0);
        map.put("location", event.getLocation() != null ? event.getLocation() : "");
        map.put("region", event.getRegion() != null ? event.getRegion() : "");
        map.put("rso", event.getRso() != null ? event.getRso() : "");
        map.put("sale", event.getSale() != null ? event.getSale() : 0);
        map.put("start_date", event.getStartDate() != null ? event.getStartDate() : "");
        map.put("store_code", event.getStore() != null ? event.getStore().getStoreCode() : "");
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

    /**
     * Save bride checklist image
     */
    public ResponseDataDTO saveBrideImage(MultipartFile file) {
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(false);

        try {
            String newFileName = this.getNewFileName(file.getOriginalFilename());
            String sep = isWindows.equalsIgnoreCase("Y") ? "\\" : "/";
            String brideImagesDir = UPLOAD_DIR + sep + "bride_images";
            String filePath = brideImagesDir + sep + newFileName;

            File targetFile = new File(filePath);
            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create directory: " + parent);
            }

            file.transferTo(targetFile);
            dto.setStatus(true);
            dto.setMessage(CommonConstants.SUCCESS_CONST);
            dto.setFilePath(filePath);
            dto.setResult(newFileName);
            log.info("Bride image saved successfully at: {}", filePath);
        } catch (Exception e) {
            log.error("Error saving bride image: {}", e.getMessage());
            dto.setMessage("Error saving image: " + e.getMessage());
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
        try {
            Event event = eventRepository.findById(eventCode).orElse(null);
            if (event == null) {
                dto.setStatus(false);
                dto.setMessage("Event not found");
                return dto;
            }
            event.setSale(Double.parseDouble(sale));
            eventRepository.save(event);
            dto.setStatus(true);
            dto.setMessage("Sale updated successfully");
        } catch (Exception e) {
            dto.setStatus(false);
            dto.setMessage("Error updating sale: " + e.getMessage());
        }
        return dto;
    }

    public ResponseDataDTO updateAdvanceOfAnEvent(String eventCode, String advance) {
        ResponseDataDTO dto = new ResponseDataDTO();
        try {
            Event event = eventRepository.findById(eventCode).orElse(null);
            if (event == null) {
                dto.setStatus(false);
                dto.setMessage("Event not found");
                return dto;
            }
            event.setAdvance(Double.parseDouble(advance));
            eventRepository.save(event);
            dto.setStatus(true);
            dto.setMessage("Advance updated successfully");
        } catch (Exception e) {
            dto.setStatus(false);
            dto.setMessage("Error updating advance: " + e.getMessage());
        }
        return dto;
    }

    public ResponseDataDTO updateGhsRgaOfAnEvent(String eventCode, String ghsRga) {
        ResponseDataDTO dto = new ResponseDataDTO();
        try {
            Event event = eventRepository.findById(eventCode).orElse(null);
            if (event == null) {
                dto.setStatus(false);
                dto.setMessage("Event not found");
                return dto;
            }
            event.setGhsOrRga(Double.parseDouble(ghsRga));
            eventRepository.save(event);
            dto.setStatus(true);
            dto.setMessage("GHS/RGA updated successfully");
        } catch (Exception e) {
            dto.setStatus(false);
            dto.setMessage("Error updating GHS/RGA: " + e.getMessage());
        }
        return dto;
    }

    public ResponseDataDTO updateGmbOfAnEvent(String eventCode, String gmb) {
        ResponseDataDTO dto = new ResponseDataDTO();
        try {
            Event event = eventRepository.findById(eventCode).orElse(null);
            if (event == null) {
                dto.setStatus(false);
                dto.setMessage("Event not found");
                return dto;
            }
            event.setGmb(Double.parseDouble(gmb));
            eventRepository.save(event);
            dto.setStatus(true);
            dto.setMessage("GMB updated successfully");
        } catch (Exception e) {
            dto.setStatus(false);
            dto.setMessage("Error updating GMB: " + e.getMessage());
        }
        return dto;
    }

    /**
     * Update event with S3 folder link after files are uploaded
     */
    public void updateEventCompletedLink(String eventId, String s3FolderUrl) {
        try {
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event != null) {
                event.setCompletedEventsDriveLink(s3FolderUrl);
                eventRepository.save(event);
                log.info("Updated event {} with S3 folder link: {}", eventId, s3FolderUrl);
            } else {
                log.warn("Event not found for updating S3 link: {}", eventId);
            }
        } catch (Exception e) {
            log.error("Failed to update event completed link", e);
        }
    }

    @Transactional
    public ResponseDataDTO changePasswordForEventManager(String storeCode, String oldPassword, String newPassword) {
        ResponseDataDTO dto = new ResponseDataDTO();
        dto.setStatus(false);
        
        try {
            // Validate input
            if (storeCode == null || storeCode.trim().isEmpty()) {
                dto.setMessage("Store code is required");
                return dto;
            }
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                dto.setMessage("Old password is required");
                return dto;
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                dto.setMessage("New password is required");
                return dto;
            }
            
            // Find user by username (storeCode)
            Optional<User> userOptional = userRepository.findByUsername(storeCode);
            if (!userOptional.isPresent()) {
                dto.setMessage("User not found with store code: " + storeCode);
                return dto;
            }
            
            User user = userOptional.get();
            
            // Verify old password
            if (!user.getPassword().equals(oldPassword)) {
                dto.setMessage("Old password is incorrect");
                return dto;
            }
            
            // Update password in database
            user.setPassword(newPassword);
            userRepository.save(user);
            
            // Update password cache (use uppercase to match login lookup)
            passwordCache.put(storeCode.toUpperCase(), newPassword);
            
            // Delete existing password history for this store (since btqCode is now primary key)
            passwordHistoryRepository.deleteById(storeCode);
            
            // Save new password change history (overwrites previous)
            PasswordHistory history = new PasswordHistory(
                storeCode,
                oldPassword,
                newPassword,
                LocalDateTime.now()
            );
            passwordHistoryRepository.save(history);
            
            dto.setStatus(true);
            dto.setMessage("Password changed successfully");
            
        } catch (Exception e) {
            dto.setStatus(false);
            dto.setMessage("Error changing password: " + e.getMessage());
        }
        
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
        if (startDateStr == null || startDateStr.trim().isEmpty() || 
            endDateStr == null || endDateStr.trim().isEmpty()) {
            return events;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startDateStr.trim(), formatter);
            LocalDate endDate = LocalDate.parse(endDateStr.trim(), formatter);

            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> event : events) {
                Object startDateObj = event.get("StartDate");
                if (startDateObj != null) {
                    try {
                        String startDateValue = startDateObj.toString();
                        LocalDate eventDate = LocalDate.parse(startDateValue, formatter);
                        
                        if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
                            filtered.add(event);
                        }
                    } catch (Exception e) {
                        // Skip events with invalid date format
                    }
                }
            }
            return filtered;
        } catch (Exception e) {
            // If date parsing fails, return all events
            return events;
        }
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
     * Get aggregated summary for RBM across all their stores
     */
    public StoreEventSummaryDTO getRbmSummary(String rbmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        List<String> storeCodes = fetchStoresByRbm(rbmUsername);
        return getAggregatedSummary(storeCodes, rbmUsername, startDate, endDate);
    }

    /**
     * Get aggregated summary for CEE across all their stores
     */
    public StoreEventSummaryDTO getCeeSummary(String ceeUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        List<String> storeCodes = fetchStoresByCee(ceeUsername);
        return getAggregatedSummary(storeCodes, ceeUsername, startDate, endDate);
    }

    /**
     * Get aggregated summary for ABM across all their stores
     */
    public StoreEventSummaryDTO getAbmSummary(String abmUsername, LocalDate startDate, LocalDate endDate) throws Exception {
        List<String> storeCodes = fetchStoresByAbm(abmUsername);
        return getAggregatedSummary(storeCodes, abmUsername, startDate, endDate);
    }

    /**
     * Aggregate summary across multiple stores
     */
    private StoreEventSummaryDTO getAggregatedSummary(List<String> storeCodes, String identifier, LocalDate startDate, LocalDate endDate) throws Exception {
        int totalEvents = 0;
        int totalInvitees = 0;
        int totalAttendees = 0;
        double totalAdvance = 0;
        double totalGhsOrRga = 0;
        double totalSale = 0;

        for (String storeCode : storeCodes) {
            StoreEventSummaryDTO storeSummary = processSingleStoreCode(storeCode, startDate, endDate);
            totalEvents += storeSummary.getTotalEvents();
            totalInvitees += storeSummary.getTotalInvitees();
            totalAttendees += storeSummary.getTotalAttendees();
            totalAdvance += storeSummary.getTotalAdvance();
            totalGhsOrRga += storeSummary.getTotalGhsOrRga();
            totalSale += storeSummary.getTotalSale();
        }

        return new StoreEventSummaryDTO(
                identifier,
                totalEvents,
                totalInvitees,
                totalAttendees,
                totalAdvance,
                totalGhsOrRga,
                totalSale
        );
    }

    /**
     * Store bride details in database and generate checklist image
     */
    public ResponseEntity<byte[]> storeBrideDetails(String brideType, String brideEvent, String brideName, String phone, String date, String email, String zipCode, String filepath) {
        log.info("Received bride details submission - Name: {}, Event: {}, Type: {}, FilePath: {}", brideName, brideEvent, brideType, filepath);

        try {
            // Validate required fields
            if (brideName == null || brideName.trim().isEmpty()) {
                log.error("✗ brideName is required but was null or empty");
                return ResponseEntity.badRequest().body("brideName is required".getBytes());
            }
            if (phone == null || phone.trim().isEmpty()) {
                log.error("✗ phone is required but was null or empty");
                return ResponseEntity.badRequest().body("phone is required".getBytes());
            }
            if (email == null || email.trim().isEmpty()) {
                log.error("✗ email is required but was null or empty");
                return ResponseEntity.badRequest().body("email is required".getBytes());
            }
            if (date == null || date.trim().isEmpty()) {
                log.error("✗ date is required but was null or empty");
                return ResponseEntity.badRequest().body("date is required".getBytes());
            }

            // Save to database
            BrideDetails brideDetails = new BrideDetails();

            // Set required fields with individual try-catch
            try {
                brideDetails.setBrideName(brideName.trim());
                log.debug("✓ Set brideName: {}", brideName.trim());
            } catch (Exception e) {
                log.error("✗ Failed to set brideName", e);
                throw new RuntimeException("Failed to set brideName: " + e.getMessage(), e);
            }

            try {
                brideDetails.setPhone(phone.trim());
                log.debug("✓ Set phone: {}", phone.trim());
            } catch (Exception e) {
                log.error("✗ Failed to set phone", e);
                throw new RuntimeException("Failed to set phone: " + e.getMessage(), e);
            }

            try {
                brideDetails.setEmail(email.trim());
                log.debug("✓ Set email: {}", email.trim());
            } catch (Exception e) {
                log.error("✗ Failed to set email", e);
                throw new RuntimeException("Failed to set email: " + e.getMessage(), e);
            }

            // Set optional fields with null checks
            try {
                if (brideType != null && !brideType.trim().isEmpty()) {
                    brideDetails.setBrideType(brideType.trim());
                    log.debug("✓ Set brideType: {}", brideType.trim());
                } else {
                    log.debug("⚠ brideType is null or empty, skipping");
                }
            } catch (Exception e) {
                log.error("✗ Failed to set brideType", e);
                // Don't throw, it's optional
            }

            try {
                if (brideEvent != null && !brideEvent.trim().isEmpty()) {
                    brideDetails.setBrideEvent(brideEvent.trim());
                    log.debug("✓ Set brideEvent: {}", brideEvent.trim());
                } else {
                    log.debug("⚠ brideEvent is null or empty, skipping");
                }
            } catch (Exception e) {
                log.error("✗ Failed to set brideEvent", e);
                // Don't throw, it's optional
            }

            try {
                if (zipCode != null && !zipCode.trim().isEmpty()) {
                    brideDetails.setZipCode(zipCode.trim());
                    log.debug("✓ Set zipCode: {}", zipCode.trim());
                } else {
                    log.debug("⚠ zipCode is null or empty, skipping");
                }
            } catch (Exception e) {
                log.error("✗ Failed to set zipCode", e);
                // Don't throw, it's optional
            }

            // Parse date string to LocalDate
            try {
                if (date != null && !date.trim().isEmpty()) {
                    LocalDate parsedDate = null;

                    // Try multiple date formats
                    DateTimeFormatter[] formatters = {
                        DateTimeFormatter.ofPattern("dd-MM-yyyy"),  // 11-12-2025
                        DateTimeFormatter.ofPattern("MM-dd-yyyy"),  // 12-11-2025
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"),  // 2025-12-11 (ISO)
                        DateTimeFormatter.ISO_LOCAL_DATE           // ISO standard
                    };

                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            parsedDate = LocalDate.parse(date.trim(), formatter);
                            log.debug("✓ Parsed date '{}' using format", date);
                            break;
                        } catch (DateTimeParseException e) {
                            // Try next format
                        }
                    }

                    if (parsedDate != null) {
                        brideDetails.setDate(parsedDate);
                        log.debug("✓ Parsed and set wedding date: {}", parsedDate);
                    } else {
                        log.warn("⚠ Could not parse date with any known format: {}", date);
                        brideDetails.setDate(null);
                    }
                } else {
                    log.warn("⚠ Date is null or empty");
                }
            } catch (Exception e) {
                log.warn("⚠ Could not parse date: {}, setting to null - Error: {}", date, e.getMessage());
                brideDetails.setDate(null);
            }

            // Save to database
            try {
                log.info("Attempting to save BrideDetails to database...");
                brideDetailsRepository.save(brideDetails);
                log.info("✓ Bride details saved successfully to database for: {}", brideName);
            } catch (Exception dbException) {
                log.error("✗ DATABASE SAVE FAILED", dbException);
                log.error("  Error type: {}", dbException.getClass().getName());
                log.error("  Error message: {}", dbException.getMessage());
                if (dbException.getCause() != null) {
                    log.error("  Root cause: {}", dbException.getCause().getMessage());
                }
                throw new RuntimeException("Database save failed: " + dbException.getMessage(), dbException);
            }

            // Generate and return the checklist image
            if (filepath != null && !filepath.trim().isEmpty()) {
                log.info("Attempting to generate checklist image from file: {}", filepath);

                try {
                    File imageFile = new File(filepath);

                    // Check if file exists
                    if (!imageFile.exists()) {
                        log.error("✗ Checklist image file NOT FOUND at path: {}", filepath);
                        log.error("  File absolute path: {}", imageFile.getAbsolutePath());
                        log.error("  Parent directory exists: {}", imageFile.getParentFile() != null && imageFile.getParentFile().exists());

                        // Return success status for database save, but no image
                        HttpHeaders headers = new HttpHeaders();
                        headers.add("X-Error-Message", "Image file not found");
                        return ResponseEntity.ok()
                            .headers(headers)
                            .body(new byte[0]);
                    }

                    log.info("✓ Image file found, size: {} bytes", imageFile.length());

                    // Read the original checklist image
                    BufferedImage originalImage = ImageIO.read(imageFile);

                    if (originalImage == null) {
                        log.error("✗ Failed to read image - ImageIO.read returned null for: {}", filepath);
                        return ResponseEntity.ok().body(new byte[0]);
                    }

                    log.info("✓ Image loaded successfully - Dimensions: {}x{}", originalImage.getWidth(), originalImage.getHeight());

                    // Add text overlay with bride details
                    BufferedImage finalImage = addBrideDetailsToImage(originalImage, brideDetails);
                    log.debug("✓ Text overlay added to image");

                    // Convert to byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(finalImage, "png", baos);
                    byte[] imageBytes = baos.toByteArray();
                    log.info("✓ Image converted to bytes - Final size: {} bytes", imageBytes.length);

                    // Set response headers for download
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_PNG);
                    headers.setContentDisposition(
                        ContentDisposition.attachment()
                            .filename("wedding-checklist-" + getCurrentTimeAsUnique() + ".png")
                            .build()
                    );

                    log.info("✓ Checklist image generated successfully for: {}", brideName);
                    return ResponseEntity.ok()
                        .headers(headers)
                        .body(imageBytes);

                } catch (IOException ioEx) {
                    log.error("✗ IO Error generating checklist image: {}", ioEx.getMessage(), ioEx);
                    log.error("  File path: {}", filepath);
                    // Return success for DB save, empty image
                    return ResponseEntity.ok().body(new byte[0]);
                } catch (Exception e) {
                    log.error("✗ Unexpected error generating checklist image: {}", e.getMessage(), e);
                    // Return success for DB save, empty image
                    return ResponseEntity.ok().body(new byte[0]);
                }
            } else {
                log.warn("⚠ No filepath provided for checklist image - returning success without image");
                return ResponseEntity.ok().body(new byte[0]);
            }

        } catch (Exception e) {
            log.error("✗ CRITICAL ERROR saving bride details or generating image", e);
            log.error("  Bride Name: {}, Event: {}, Type: {}", brideName, brideEvent, brideType);
            log.error("  Stack trace: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Add bride details as text overlay on checklist image
     */
    private BufferedImage addBrideDetailsToImage(BufferedImage originalImage, BrideDetails brideDetails) {
        try {
            // Create a copy of the original image
            BufferedImage resultImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g2d = resultImage.createGraphics();

            // Draw original image
            g2d.drawImage(originalImage, 0, 0, null);

            // Enable anti-aliasing for better text quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Set font and color (Tanishq brand color)
            int fontSize = Math.max(20, originalImage.getWidth() / 40); // Responsive font size
            Font font = new Font("Arial", Font.BOLD, fontSize);
            g2d.setFont(font);
            g2d.setColor(new Color(131, 39, 41)); // Tanishq red #832729

            // Calculate position for text (bottom of image with padding)
            int textY = originalImage.getHeight() - 150;
            int textX = 50;
            int lineHeight = fontSize + 10;

            // Add semi-transparent background for text readability
            g2d.setColor(new Color(255, 255, 255, 200)); // White with transparency
            g2d.fillRoundRect(textX - 20, textY - 30, originalImage.getWidth() - 100, 140, 20, 20);

            // Draw text with details
            g2d.setColor(new Color(131, 39, 41)); // Tanishq red
            g2d.drawString("Name: " + brideDetails.getBrideName(), textX, textY);

            g2d.setColor(new Color(53, 53, 53)); // Dark gray for other details
            Font smallFont = new Font("Arial", Font.PLAIN, fontSize - 4);
            g2d.setFont(smallFont);

            textY += lineHeight;
            g2d.drawString("Event: " + brideDetails.getBrideEvent() + " (" + brideDetails.getBrideType() + " Style)", textX, textY);

            if (brideDetails.getDate() != null) {
                textY += lineHeight;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                g2d.drawString("Wedding Date: " + brideDetails.getDate().format(formatter), textX, textY);
            }

            textY += lineHeight;
            g2d.drawString("Contact: " + brideDetails.getPhone() + " | " + brideDetails.getEmail(), textX, textY);

            g2d.dispose();

            return resultImage;

        } catch (Exception e) {
            log.error("Error adding text overlay to image: {}", e.getMessage(), e);
            return originalImage; // Return original if text overlay fails
        }
    }

    // Keep existing methods that work (image processing, etc.)
    // ... (keeping existing helper methods)
}
