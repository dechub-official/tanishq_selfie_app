package com.dechub.tanishq.controller;

import com.dechub.tanishq.config.StoreSummaryCache;
import com.dechub.tanishq.dto.eventsDto.*;
import com.dechub.tanishq.security.StoreContextValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dechub.tanishq.service.TanishqPageService;
import com.dechub.tanishq.service.events.EventQrCodeService;
import com.dechub.tanishq.service.storage.StorageService;
import com.dechub.tanishq.util.APIResponseBuilder;
import com.dechub.tanishq.util.ResponseDataDTO;
import com.opencsv.CSVWriter;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
public class EventsController {

    private static final Logger log = LoggerFactory.getLogger(EventsController.class);

    @Autowired
    private TanishqPageService tanishqPageService;

    @Autowired
    private StoreSummaryCache storeSummaryCache;

    @Autowired
    private EventQrCodeService eventQrCodeService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private StoreContextValidator storeContextValidator;

    /**
     * Serve the events main page (Create Event)
     */
    @GetMapping("")
    public ModelAndView showEventsPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("forward:/events.html");
        return modelAndView;
    }

    @PostMapping("/login")
    public ResponseEntity<?> eventsLogin(@Valid @RequestBody LoginDTO loginDTO, HttpSession session) throws Exception {
        String code = loginDTO.getCode();
        String password = loginDTO.getPassword();

        // Authenticate user via service
        EventsLoginResponseDTO response = tanishqPageService.eventsLogin(code, password);

        if (response.isStatus()) {
            // Determine user type based on login code
            String userType = determineUserType(code.toUpperCase());

            // Store authentication in session - THIS IS THE SECURITY FIX
            storeContextValidator.setAuthenticatedUser(session, code.toUpperCase(), userType);

            log.info("Successful login for user: {} with type: {}", code.toUpperCase(), userType);

            // SECURITY FIX: Return ONLY success status, NO user data in response
            // User data will be fetched via /api/me endpoint
            Map<String, Object> secureResponse = new HashMap<>();
            secureResponse.put("success", true);
            secureResponse.put("message", "Login successful");
            return ResponseEntity.ok(secureResponse);
        } else {
            log.warn("Failed login attempt for code: {}", code);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", response.getMessage() != null ? response.getMessage() : "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Determine user type based on login code pattern
     */
    private String determineUserType(String code) {
        if (code.contains("-CEE")) {
            return "CEE";
        } else if (code.contains("-ABM")) {
            return "ABM";
        } else if (code.contains("-RBM") || code.matches("^(EAST|WEST|NORTH|SOUTH)\\d+$")) {
            return "RBM";
        } else if (code.contains("CORP-") || code.contains("-CORP")) {
            return "CORPORATE";
        } else if (code.matches("^(east|west|north|south)\\d+[ab]?$")) {
            return "REGIONAL";
        } else {
            return "STORE";
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (session != null) {
            String user = storeContextValidator.getAuthenticatedUser(session);
            storeContextValidator.clearAuthentication(session);
            log.info("User '{}' logged out successfully", user);
            response.put("status", true);
            response.put("message", "Logged out successfully");
        } else {
            response.put("status", false);
            response.put("message", "No active session");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * SECURITY FIX: Get authenticated user context from server-side session
     * This endpoint returns user data ONLY if the user is authenticated via session
     * Frontend must call this after login to get user details securely
     *
     * @param session HTTP session
     * @return User context with authorized stores or 401 if not authenticated
     */
    @GetMapping("/api/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        // Check if user is authenticated
        if (!storeContextValidator.isAuthenticated(session)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("authenticated", false);
            errorResponse.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        try {
            String username = storeContextValidator.getAuthenticatedUser(session);
            String userType = (String) session.getAttribute("userType");
            Long loginTimestamp = (Long) session.getAttribute("loginTimestamp");

            // Build secure user context response
            Map<String, Object> userContext = new HashMap<>();
            userContext.put("authenticated", true);
            userContext.put("username", username);
            userContext.put("userType", userType);
            userContext.put("loginTime", loginTimestamp);

            // Get user-specific details based on type
            if ("STORE".equals(userType)) {
                // Get store details for store users
                Map<String, Object> storeDetails = tanishqPageService.getStoreDetails(username);
                if (storeDetails != null) {
                    userContext.put("storeData", storeDetails);
                }
                userContext.put("authorizedStores", Collections.singletonList(username));
            } else if ("ABM".equals(userType) || "RBM".equals(userType) || "CEE".equals(userType) || "CORPORATE".equals(userType)) {
                // Get list of authorized stores for manager users
                List<String> authorizedStores = getAuthorizedStoresForUser(username, userType);
                userContext.put("authorizedStores", authorizedStores);
                userContext.put("totalStores", authorizedStores.size());
            } else if ("REGIONAL".equals(userType)) {
                userContext.put("region", username);
                List<String> regionalStores = tanishqPageService.getStoresByRegionCode(username);
                userContext.put("authorizedStores", regionalStores);
            }

            log.info("User context fetched for: {} (type: {})", username, userType);
            return ResponseEntity.ok(userContext);

        } catch (Exception e) {
            log.error("Error fetching user context", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("authenticated", true);
            errorResponse.put("error", "Failed to load user context");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Helper method to get authorized stores for a user based on their type
     */
    private List<String> getAuthorizedStoresForUser(String username, String userType) throws Exception {
        switch (userType.toUpperCase()) {
            case "ABM":
                return tanishqPageService.fetchStoresByAbm(username);
            case "RBM":
                return tanishqPageService.fetchStoresByRbm(username);
            case "CEE":
                return tanishqPageService.fetchStoresByCee(username);
            case "CORPORATE":
                return tanishqPageService.fetchStoresByCorporate(username);
            default:
                return Collections.emptyList();
        }
    }

    @GetMapping("/dowload-qr/{id}")
    private QrResponseDTO downloadQr(@PathVariable("id") String eventId){
        QrResponseDTO qrResponseDTO = new QrResponseDTO();
        try {
            // Use dedicated Event QR Code Service (NOT Greeting QR service)
            String qrCodeBase64 = eventQrCodeService.generateEventQrCode(eventId);
            qrResponseDTO.setStatus(true);
            qrResponseDTO.setQrData("data:image/png;base64," + qrCodeBase64);
        } catch (Exception e) {
            qrResponseDTO.setStatus(false);
            qrResponseDTO.setQrData("Error generating QR code: " + e.getMessage());
        }
        return qrResponseDTO;
    }

    /**
     * Handle QR code scan - show attendee registration form
     * When users scan the QR code, they are redirected here
     */
    @GetMapping("/customer/{eventId}")
    public ModelAndView showAttendeeForm(@PathVariable("eventId") String eventId) {
        // Forward to the events.html page, React Router will handle the routing
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("forward:/events.html");
        return modelAndView;
    }

    /**
     * UNIFIED endpoint that accepts BOTH form-data and JSON
     * This endpoint intelligently detects the content type and handles accordingly
     * NO FRONTEND CHANGES REQUIRED - works with both old and new implementations
     */
    @PostMapping(path = "/upload", produces = "application/json")
    public QrResponseDTO storeEventsDetails(
            HttpServletRequest httpRequest,
            @RequestParam(value = "code",required = false) String code,
            @RequestParam(value = "file",required = false) MultipartFile file,
            @RequestParam(value ="description",required = false) String description,
            @RequestParam(value ="singalInvite",required = false, defaultValue = "false") String isSingleCustomerStr,
            @RequestParam(value ="eventName",required = false) String eventName,
            @RequestParam(value ="eventType",required = false) String eventType,
            @RequestParam(value ="eventSubType",required = false) String eventSubType,
            @RequestParam(value ="RSO",required = false) String rso,
            @RequestParam(value ="date",required = false) String startDate,
            @RequestParam(value ="time",required = false) String startTime,
            @RequestParam(value ="image",required = false) String image,
            @RequestParam(value ="location",required = false) String location,
            @RequestParam(value ="Community",required = false) String community,
            @RequestParam(value ="customerName",required = false) String name,
            @RequestParam(value ="customerContact",required = false) String contact,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "sale", required = false) Integer sale,
            @RequestParam(value = "advance", required = false) Integer advance,
            @RequestParam(value = "ghsOrRga", required = false) Integer ghsOrRga,
            @RequestParam(value = "gmb", required = false) Integer gmb,
            @RequestParam(value = "diamondAwareness", required = false, defaultValue = "false") String diamondAwarenessStr,
            @RequestParam(value = "ghsFlag", required = false, defaultValue = "false") String ghsFlagStr,
            @RequestBody(required = false) String jsonBody,
            HttpSession session
    ) {
        // Check if request is JSON by examining content-type or if jsonBody is present
        String contentType = httpRequest.getContentType();
        boolean isJsonRequest = (contentType != null && contentType.contains("application/json")) ||
                                (jsonBody != null && jsonBody.trim().startsWith("{"));

        log.info("Received upload request - Content-Type: {}, isJSON: {}", contentType, isJsonRequest);

        // If JSON request, parse and delegate to JSON handler
        if (isJsonRequest && jsonBody != null && !jsonBody.trim().isEmpty()) {
            return handleJsonUpload(jsonBody, session);
        }

        // Otherwise, handle as form-data (existing logic)
        boolean isSingleCustomer = "true".equalsIgnoreCase(isSingleCustomerStr);
        boolean diamondAwareness = "true".equalsIgnoreCase(diamondAwarenessStr);
        boolean ghsFlag = "true".equalsIgnoreCase(ghsFlagStr);
        // SECURITY: Validate authentication first
        if (!storeContextValidator.isAuthenticated(session)) {
            QrResponseDTO errorResponse = new QrResponseDTO();
            errorResponse.setStatus(false);
            errorResponse.setQrData("Authentication required. Please log in.");
            log.warn("Unauthorized upload attempt - no authentication");
            return errorResponse;
        }

        // SECURITY FIX: Use authenticated user's store code from session instead of trusting frontend
        // This prevents users from tampering with the code parameter
        String authenticatedUser = storeContextValidator.getAuthenticatedUser(session);
        String userType = (String) session.getAttribute("userType");

        // If code is null or empty, use the authenticated user as the store code
        // This fixes the issue where frontend doesn't send the code parameter
        if (code == null || code.trim().isEmpty()) {
            code = authenticatedUser;
            log.info("Using authenticated user '{}' as store code for event creation", code);
        }

        // For store-level users, always override with their authenticated store code
        // This prevents privilege escalation where a store tries to create events for another store
        if ("STORE".equals(userType)) {
            if (!authenticatedUser.equalsIgnoreCase(code)) {
                log.warn("SECURITY: Store user '{}' attempted to create event for different store '{}', overriding",
                         authenticatedUser, code);
            }
            code = authenticatedUser; // Always use authenticated store code for store users
        }

        // Validate that the user has access to this store
        if (!storeContextValidator.validateStoreAccess(session, code)) {
            QrResponseDTO errorResponse = new QrResponseDTO();
            errorResponse.setStatus(false);
            errorResponse.setQrData("Access denied. You are not authorized to create events for this store.");
            log.error("SECURITY ALERT: Unauthorized store access attempt for store: {} by user: {} (type: {})",
                     code, authenticatedUser, userType);
            return errorResponse;
        }

        // INPUT VALIDATION: Validate required fields and formats
        QrResponseDTO validationError = validateUploadInputs(code, eventName, eventType, startDate, startTime, name, contact);
        if (validationError != null) {
            return validationError;
        }

        EventsDetailDTO eventsDetailDTO = new EventsDetailDTO();
        if(file==null||file.isEmpty()){
            eventsDetailDTO.setSingleCustomer(true);
        }else{
            eventsDetailDTO.setSingleCustomer(isSingleCustomer);

        }
        eventsDetailDTO.setStoreCode(code);
        eventsDetailDTO.setDescription(description);
        eventsDetailDTO.setFile(file);
        eventsDetailDTO.setEventName(eventName);
        eventsDetailDTO.setEventType(eventType);
        eventsDetailDTO.setEventSubType(eventSubType);
        eventsDetailDTO.setCommunity(community);
        eventsDetailDTO.setImage(image);
        eventsDetailDTO.setRso(rso);
        eventsDetailDTO.setLocation(location);
        eventsDetailDTO.setStartDate(startDate);

        // Handle optional time field - use default if empty
        if (startTime == null || startTime.trim().isEmpty()) {
            startTime = "00:00"; // Default time if not provided
            log.debug("Time not provided for event, using default: {}", startTime);
        }
        eventsDetailDTO.setStartTime(startTime);
        eventsDetailDTO.setName(name);
        eventsDetailDTO.setContact(contact);

        eventsDetailDTO.setRegion(region);
        eventsDetailDTO.setSale(sale);
        eventsDetailDTO.setAdvance(advance);
        eventsDetailDTO.setGhsOrRga(ghsOrRga);
        eventsDetailDTO.setGmb(gmb);
        eventsDetailDTO.setDiamondAwareness(diamondAwareness);
        eventsDetailDTO.setGhsFlag(ghsFlag);

        return tanishqPageService.storeEventsDetails(eventsDetailDTO);
    }

    /**
     * NEW JSON-based endpoint for event creation
     * Accepts JSON payload from frontend instead of form data
     * This is an alternative endpoint - the main /upload endpoint now handles both JSON and form-data
     */
    @PostMapping(path = "/upload-json", produces = "application/json", consumes = "application/json")
    public QrResponseDTO storeEventsDetailsJson(
            @Valid @RequestBody EventCreationRequest request,
            HttpSession session
    ) {
        log.info("Received JSON event creation request via /upload-json for store: {}, eventName: {}, eventType: {}",
                request.getStoreCode(), request.getEventName(), request.getEventType());

        // Use common JSON processing logic
        return processJsonEventCreation(request, session);
    }

    /**
     * Handle JSON upload by parsing the JSON string into EventCreationRequest
     * This is called from the unified /upload endpoint when JSON content is detected
     */
    private QrResponseDTO handleJsonUpload(String jsonBody, HttpSession session) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            EventCreationRequest request = objectMapper.readValue(jsonBody, EventCreationRequest.class);

            log.info("Parsed JSON request - storeCode: {}, eventName: {}, eventType: {}",
                    request.getStoreCode(), request.getEventName(), request.getEventType());

            // Use the same logic as /upload-json endpoint
            return processJsonEventCreation(request, session);

        } catch (Exception e) {
            log.error("Failed to parse JSON request", e);
            QrResponseDTO errorResponse = new QrResponseDTO();
            errorResponse.setStatus(false);
            errorResponse.setQrData("Invalid JSON format: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Process JSON event creation request (used by both /upload and /upload-json)
     */
    private QrResponseDTO processJsonEventCreation(EventCreationRequest request, HttpSession session) {
        // SECURITY: Validate authentication first
        if (!storeContextValidator.isAuthenticated(session)) {
            QrResponseDTO errorResponse = new QrResponseDTO();
            errorResponse.setStatus(false);
            errorResponse.setQrData("Authentication required. Please log in.");
            log.warn("Unauthorized upload attempt - no authentication");
            return errorResponse;
        }

        // SECURITY FIX: Use authenticated user's store code from session
        String authenticatedUser = storeContextValidator.getAuthenticatedUser(session);
        String userType = (String) session.getAttribute("userType");

        String storeCode = request.getStoreCode();

        // If storeCode is null or empty, use the authenticated user as the store code
        if (storeCode == null || storeCode.trim().isEmpty()) {
            storeCode = authenticatedUser;
            request.setStoreCode(storeCode);
            log.info("Using authenticated user '{}' as store code for event creation", storeCode);
        }

        // For store-level users, always override with their authenticated store code
        if ("STORE".equals(userType)) {
            if (!authenticatedUser.equalsIgnoreCase(storeCode)) {
                log.warn("SECURITY: Store user '{}' attempted to create event for different store '{}', overriding",
                        authenticatedUser, storeCode);
            }
            storeCode = authenticatedUser;
            request.setStoreCode(storeCode);
        }

        // Validate that the user has access to this store
        if (!storeContextValidator.validateStoreAccess(session, storeCode)) {
            QrResponseDTO errorResponse = new QrResponseDTO();
            errorResponse.setStatus(false);
            errorResponse.setQrData("Access denied. You are not authorized to create events for this store.");
            log.error("SECURITY ALERT: Unauthorized store access attempt for store: {} by user: {} (type: {})",
                    storeCode, authenticatedUser, userType);
            return errorResponse;
        }

        // INPUT VALIDATION: Additional custom validation
        QrResponseDTO validationError = validateJsonUploadInputs(request);
        if (validationError != null) {
            return validationError;
        }

        // Map JSON request to EventsDetailDTO
        EventsDetailDTO eventsDetailDTO = mapRequestToDTO(request);

        log.info("Creating event: {} for store: {} on date: {}",
                eventsDetailDTO.getEventName(), eventsDetailDTO.getStoreCode(), eventsDetailDTO.getStartDate());

        return tanishqPageService.storeEventsDetails(eventsDetailDTO);
    }

    /**
     * Map EventCreationRequest (JSON from frontend) to EventsDetailDTO (internal DTO)
     */
    private EventsDetailDTO mapRequestToDTO(EventCreationRequest request) {
        EventsDetailDTO dto = new EventsDetailDTO();

        dto.setStoreCode(request.getStoreCode());
        dto.setEventType(request.getEventType());
        dto.setEventSubType(request.getEventSubType());
        dto.setEventName(request.getEventName()); // This is now properly set from frontend
        dto.setRso(request.getRSO());
        dto.setStartDate(request.getDate());

        // Handle optional time field - use default if empty
        String time = request.getTime();
        if (time == null || time.trim().isEmpty()) {
            time = "00:00"; // Default time if not provided
            log.debug("Time not provided, using default: {}", time);
        }
        dto.setStartTime(time);

        dto.setLocation(request.getLocation());
        dto.setCommunity(request.getCommunity());
        dto.setImage(request.getImage());
        dto.setDescription(request.getDescription());

        // Convert string booleans to actual booleans
        dto.setSingleCustomer(request.isSingleInvite());
        dto.setDiamondAwareness(request.isDiamondAwareness());
        dto.setGhsFlag(request.isGhsFlag());

        dto.setName(request.getCustomerName());
        dto.setContact(request.getCustomerContact());

        dto.setRegion(request.getRegion());
        dto.setSale(request.getSale());
        dto.setAdvance(request.getAdvance());
        dto.setGhsOrRga(request.getGhsOrRga());
        dto.setGmb(request.getGmb());

        // File is null for JSON requests (file uploads require multipart)
        dto.setFile(null);

        return dto;
    }

    /**
     * Validate JSON upload input parameters
     */
    private QrResponseDTO validateJsonUploadInputs(EventCreationRequest request) {
        QrResponseDTO errorResponse = new QrResponseDTO();
        errorResponse.setStatus(false);

        // Validate phone number if provided
        if (request.getCustomerContact() != null && !request.getCustomerContact().trim().isEmpty()) {
            if (!com.dechub.tanishq.util.InputValidator.isValidPhone(request.getCustomerContact())) {
                errorResponse.setQrData("Invalid phone number format. Must be 10 digits starting with 6-9");
                return errorResponse;
            }
        }

        // Validate name if provided
        if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
            if (!com.dechub.tanishq.util.InputValidator.isValidName(request.getCustomerName())) {
                errorResponse.setQrData("Invalid name format. Name must be 2-100 characters with only letters and spaces");
                return errorResponse;
            }
        }

        // eventName validation is now handled by @Valid @NotBlank annotation
        // No need to check for null or empty here

        return null; // No validation errors
    }

    /**
     * Validate upload input parameters
     */
    private QrResponseDTO validateUploadInputs(String code, String eventName, String eventType,
                                                String startDate, String startTime, String name, String contact) {
        QrResponseDTO errorResponse = new QrResponseDTO();
        errorResponse.setStatus(false);

        // Validate required fields
        if (code == null || code.trim().isEmpty()) {
            errorResponse.setQrData("Store code is required");
            return errorResponse;
        }
        if (eventName == null || eventName.trim().isEmpty()) {
            errorResponse.setQrData("Event name is required");
            return errorResponse;
        }
        if (eventType == null || eventType.trim().isEmpty()) {
            errorResponse.setQrData("Event type is required");
            return errorResponse;
        }
        if (startDate == null || startDate.trim().isEmpty()) {
            errorResponse.setQrData("Start date is required");
            return errorResponse;
        }
        // Note: startTime is now optional, a default value will be set if empty

        // Validate lengths
        if (code.length() > 50) {
            errorResponse.setQrData("Store code must not exceed 50 characters");
            return errorResponse;
        }
        if (eventName.length() > 200) {
            errorResponse.setQrData("Event name must not exceed 200 characters");
            return errorResponse;
        }
        if (eventType.length() > 100) {
            errorResponse.setQrData("Event type must not exceed 100 characters");
            return errorResponse;
        }

        // Validate phone number if provided
        if (contact != null && !contact.trim().isEmpty()) {
            if (!com.dechub.tanishq.util.InputValidator.isValidPhone(contact)) {
                errorResponse.setQrData("Invalid phone number format. Must be 10 digits starting with 6-9");
                return errorResponse;
            }
        }

        // Validate name if provided
        if (name != null && !name.trim().isEmpty()) {
            if (!com.dechub.tanishq.util.InputValidator.isValidName(name)) {
                errorResponse.setQrData("Invalid name format. Name must be 2-100 characters with only letters and spaces");
                return errorResponse;
            }
        }

        return null; // No validation errors
    }

    @PostMapping("/attendees")
    public ResponseDataDTO storeAttendeesData(@RequestParam(name = "eventId",required = false) String eventId,
                                              @RequestParam(name="name",required = false) String name,
                                              @RequestParam(name="phone",required = false) String phone,
                                              @RequestParam(name="like",required = false) String like,
                                              @RequestParam(name="firstTimeAtTanishq",required = false) boolean firstTimeAtTanishq,
                                              @RequestParam(name="file",required = false) MultipartFile file,
                                              @RequestParam(name = "rsoName",required = false) String rsoName){

        log.info("Received attendee submission - EventId: {}, Name: {}, Phone: {}", eventId, name, phone);

        // Validate required fields
        ResponseDataDTO errorResponse = new ResponseDataDTO();
        if (eventId == null || eventId.trim().isEmpty()) {
            log.error("EventId is null or empty");
            errorResponse.setStatus(false);
            errorResponse.setMessage("Event ID is required");
            return errorResponse;
        }

        // INPUT VALIDATION: Validate phone number format
        if (phone != null && !phone.trim().isEmpty()) {
            if (!com.dechub.tanishq.util.InputValidator.isValidPhone(phone)) {
                log.error("Invalid phone number format: {}", phone);
                errorResponse.setStatus(false);
                errorResponse.setMessage("Invalid phone number format. Must be 10 digits starting with 6-9");
                return errorResponse;
            }
        }

        // INPUT VALIDATION: Validate name format and length
        if (name != null && !name.trim().isEmpty()) {
            if (!com.dechub.tanishq.util.InputValidator.isValidName(name)) {
                log.error("Invalid name format: {}", name);
                errorResponse.setStatus(false);
                errorResponse.setMessage("Invalid name format. Name must be 2-100 characters with only letters and spaces");
                return errorResponse;
            }
        }

        // INPUT VALIDATION: Validate field lengths
        if (eventId.length() > 50) {
            errorResponse.setStatus(false);
            errorResponse.setMessage("Event ID must not exceed 50 characters");
            return errorResponse;
        }
        if (like != null && like.length() > 500) {
            errorResponse.setStatus(false);
            errorResponse.setMessage("Like field must not exceed 500 characters");
            return errorResponse;
        }
        if (rsoName != null && rsoName.length() > 100) {
            errorResponse.setStatus(false);
            errorResponse.setMessage("RSO name must not exceed 100 characters");
            return errorResponse;
        }

        AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
        attendeesDetailDTO.setId(eventId);
        attendeesDetailDTO.setLike(like);
        attendeesDetailDTO.setPhone(phone);
        attendeesDetailDTO.setName(name);
        attendeesDetailDTO.setFirstTimeAtTanishq(firstTimeAtTanishq);
        attendeesDetailDTO.setFile(file);
        attendeesDetailDTO.setRsoName(rsoName);
        attendeesDetailDTO.setBulkUpload(file != null && !file.isEmpty());

        log.debug("Attendee DTO: {}", attendeesDetailDTO);

        return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
    }

    @PostMapping("/getevents")
    public CompletedEventsResponseDTO getAllCompletedEvents(@Valid @RequestBody storeCodeDataDTO storeCodeDataDTO, HttpSession session){
        // SECURITY: Validate authentication and authorization
        if (!storeContextValidator.isAuthenticated(session)) {
            CompletedEventsResponseDTO errorResponse = new CompletedEventsResponseDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Authentication required. Please log in.");
            log.warn("Unauthorized getevents attempt - no authentication");
            return errorResponse;
        }

        if (!storeContextValidator.validateStoreAccess(session, storeCodeDataDTO.getStoreCode())) {
            CompletedEventsResponseDTO errorResponse = new CompletedEventsResponseDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Access denied. You are not authorized to view events for this store.");
            log.error("SECURITY ALERT: Unauthorized getevents attempt for store: {} by user: {}",
                     storeCodeDataDTO.getStoreCode(), storeContextValidator.getAuthenticatedUser(session));
            return errorResponse;
        }

        return tanishqPageService.getAllCompletedEvents(
            storeCodeDataDTO.getStoreCode(), 
            storeCodeDataDTO.getStartDate(), 
            storeCodeDataDTO.getEndDate()
        );
    }

    @GetMapping("/getStoresByRegion/{region}")
    public List<storeCodeDataDTO> getStoresByRegion(@PathVariable String region) {
        return tanishqPageService.getStoresByRegion(region);
    }

    @PostMapping("/updateSaleOfAnEvent")
    public ResponseDataDTO updateSaleOfAnEvent(@RequestParam String eventCode,
                                               @RequestParam String sale,
                                               HttpSession session){
        // SECURITY: Validate authentication and authorization
        if (!storeContextValidator.isAuthenticated(session)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Authentication required. Please log in.");
            return errorResponse;
        }

        if (!storeContextValidator.validateEventAccess(session, eventCode)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Access denied. You are not authorized to update this event.");
            log.error("SECURITY ALERT: Unauthorized updateSale attempt for event: {} by user: {}",
                     eventCode, storeContextValidator.getAuthenticatedUser(session));
            return errorResponse;
        }

        return tanishqPageService.updateSaleOfAnEvent(eventCode, sale);
    }
    @PostMapping("/updateAdvanceOfAnEvent")
    public ResponseDataDTO updateAdvanceOfAnEvent(@RequestParam String eventCode,
                                                  @RequestParam String advance,
                                                  HttpSession session){
        // SECURITY: Validate authentication and authorization
        if (!storeContextValidator.isAuthenticated(session)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Authentication required. Please log in.");
            return errorResponse;
        }

        if (!storeContextValidator.validateEventAccess(session, eventCode)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Access denied. You are not authorized to update this event.");
            log.error("SECURITY ALERT: Unauthorized updateAdvance attempt for event: {} by user: {}",
                     eventCode, storeContextValidator.getAuthenticatedUser(session));
            return errorResponse;
        }

        return tanishqPageService.updateAdvanceOfAnEvent(eventCode, advance);
    }
    @PostMapping("/updateGhsRgaOfAnEvent")
    public ResponseDataDTO updateGhsRgaOfAnEvent(@Valid @RequestBody UpdateEventGhsRgaDTO updateDTO,
                                                 HttpSession session){
        String eventCode = updateDTO.getEventCode();

        // SECURITY: Validate authentication and authorization
        if (!storeContextValidator.isAuthenticated(session)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Authentication required. Please log in.");
            return errorResponse;
        }

        if (!storeContextValidator.validateEventAccess(session, eventCode)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Access denied. You are not authorized to update this event.");
            log.error("SECURITY ALERT: Unauthorized updateGhsRga attempt for event: {} by user: {}",
                     eventCode, storeContextValidator.getAuthenticatedUser(session));
            return errorResponse;
        }

        return tanishqPageService.updateGhsRgaOfAnEvent(eventCode, String.valueOf(updateDTO.getGhsRga()));
    }
    @PostMapping("/updateGmbOfAnEvent")
    public ResponseDataDTO updateGmbOfAnEvent(@Valid @RequestBody UpdateEventGmbDTO updateDTO,
                                              HttpSession session){
        String eventCode = updateDTO.getEventCode();

        // SECURITY: Validate authentication and authorization
        if (!storeContextValidator.isAuthenticated(session)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Authentication required. Please log in.");
            return errorResponse;
        }

        if (!storeContextValidator.validateEventAccess(session, eventCode)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Access denied. You are not authorized to update this event.");
            log.error("SECURITY ALERT: Unauthorized updateGmb attempt for event: {} by user: {}",
                     eventCode, storeContextValidator.getAuthenticatedUser(session));
            return errorResponse;
        }

        return tanishqPageService.updateGmbOfAnEvent(eventCode, String.valueOf(updateDTO.getGmb()));
    }

    @PostMapping("/getinvitedmember")
    public ResponseEntity<ResponseDataDTO> getInvitedMember(@Valid @RequestBody EventCodeDTO eventCodeDTO,
                                                            HttpSession session) throws Exception {
        String eventCode = eventCodeDTO.getEventCode();

        // SECURITY: Validate authentication and authorization
        if (!storeContextValidator.isAuthenticated(session)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Authentication required. Please log in.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        if (!storeContextValidator.validateEventAccess(session, eventCode)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Access denied. You are not authorized to view this event's invitees.");
            log.error("SECURITY ALERT: Unauthorized getInvitedMember attempt for event: {} by user: {}",
                     eventCode, storeContextValidator.getAuthenticatedUser(session));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        List<?> list = tanishqPageService.getInvitedMember(eventCode);
        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(true);
        response.setResult(list);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/uploadCompletedEvents")
    public ResponseDataDTO uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                       @RequestParam("eventId") String eventId) {
        ResponseDataDTO responseDataDTO = new ResponseDataDTO();

        try {
            log.info("Uploading {} files to S3 for event: {}", files.size(), eventId);

            // Validate files
            List<MultipartFile> validFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!isAllowedFileType(file.getOriginalFilename())) {
                    log.warn("Skipping file with invalid type: {}", file.getOriginalFilename());
                    continue;
                }
                validFiles.add(file);
            }

            if (validFiles.isEmpty()) {
                responseDataDTO.setStatus(false);
                responseDataDTO.setMessage("No valid files to upload");
                return responseDataDTO;
            }

            // Upload files to S3 in parallel
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(validFiles.size(), 10));
            List<CompletableFuture<String>> uploadFutures = new ArrayList<>();

            for (MultipartFile file : validFiles) {
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        // Upload using StorageService and get URL
                        return storageService.uploadEventFile(file, eventId);
                    } catch (Exception e) {
                        log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                        return null;
                    }
                }, executor);

                uploadFutures.add(future);
            }

            // Wait for all uploads to complete
            List<String> uploadedUrls = uploadFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(url -> url != null)
                    .collect(Collectors.toList());

            executor.shutdown();

            // Get storage folder URL for the event
            String folderUrl = storageService.getEventFolderUrl(eventId);

            // Update event with folder link
            try {
                tanishqPageService.updateEventCompletedLink(eventId, folderUrl);
            } catch (Exception e) {
                log.error("Failed to update event completed link", e);
            }

            boolean allSuccess = uploadedUrls.size() == validFiles.size();
            responseDataDTO.setStatus(allSuccess);
            responseDataDTO.setMessage(
                allSuccess
                ? String.format("All %d files uploaded successfully to S3", uploadedUrls.size())
                : String.format("Uploaded %d of %d files to S3", uploadedUrls.size(), validFiles.size())
            );
            responseDataDTO.setResult(uploadedUrls);

            log.info("Upload complete: {} of {} files uploaded for event {}",
                    uploadedUrls.size(), validFiles.size(), eventId);

        } catch (Exception e) {
            log.error("Error uploading files to S3", e);
            responseDataDTO.setStatus(false);
            responseDataDTO.setMessage("Error occurred: " + e.getMessage());
        }

        return responseDataDTO;
    }

    private boolean isAllowedFileType(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return false;
        }
        String[] blacklist = {".php",".html",".htaccess",".pgif",".inc",".phar",".ctp",".module",".pht",".phtm",".asp",".ashx", ".asmx", ".aspq", ".axd", ".cshtm", ".cshtml"," .rem", ".soap", ".vbhtm"," .vbhtml", ".asa"," .cer", ".jsp", ".jspx", ".jsw", ".jsv", ".jspf","cfm", ".cfml", ".cfc", ".dbm"," .pl", ".cgi"};
        for (String blacklistedWord : blacklist) {
            if (originalFilename.toLowerCase().contains(blacklistedWord)) {
                return false;
            }
        }

        return true;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";  // No file extension found
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }


    @PostMapping("/changePassword")
    public ResponseDataDTO changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO,
                                          HttpSession session){
        // SECURITY: Validate authentication and authorization
        if (!storeContextValidator.isAuthenticated(session)) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Authentication required. Please log in.");
            log.warn("Unauthorized changePassword attempt - no authentication");
            return errorResponse;
        }

        if (!storeContextValidator.validateStoreAccess(session, changePasswordDTO.getStoreCode())) {
            ResponseDataDTO errorResponse = new ResponseDataDTO();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Access denied. You are not authorized to change password for this store.");
            log.error("SECURITY ALERT: Unauthorized changePassword attempt for store: {} by user: {}",
                     changePasswordDTO.getStoreCode(), storeContextValidator.getAuthenticatedUser(session));
            return errorResponse;
        }

        return tanishqPageService.changePasswordForEventManager(
            changePasswordDTO.getStoreCode(),
            changePasswordDTO.getOldPassword(),
            changePasswordDTO.getNewPassword(),
            changePasswordDTO.getConfirmPassword()
        );
    }

    @GetMapping("/getPasswordHint")
    public ResponseDataDTO getPasswordHint(@RequestParam String storeCode){
        return tanishqPageService.getPasswordHintForStore(storeCode);
    }

    @PostMapping("/abm_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginAbm(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
        }

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateAbm(username, password);

        if (user.isPresent()) {
            // Store authentication in session
            storeContextValidator.setAuthenticatedUser(session, username, "ABM");
            log.info("Successful ABM login for user: {}", username);

            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            log.warn("Failed ABM login attempt for username: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
        }
    }

    @PostMapping("/rbm_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginRbm(@Valid @RequestBody CredentialsDTO credentials, HttpSession session) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateRbm(username, password);

        if (user.isPresent()) {
            // Store authentication in session
            storeContextValidator.setAuthenticatedUser(session, username, "RBM");
            log.info("Successful RBM login for user: {}", username);

            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            log.warn("Failed RBM login attempt for username: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
        }
    }


    @PostMapping("/cee_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginCee(@Valid @RequestBody CredentialsDTO credentials, HttpSession session) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateCee(username, password);

        if (user.isPresent()) {
            // Store authentication in session
            storeContextValidator.setAuthenticatedUser(session, username, "CEE");
            log.info("Successful CEE login for user: {}", username);

            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            log.warn("Failed CEE login attempt for username: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
        }
    }

    @PostMapping("/corporate_login")
    public ResponseEntity<?> loginCorporate(@Valid @RequestBody CredentialsDTO credentials, HttpSession session) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateCorporate(username, password);

        if (user.isPresent()) {
            // SECURITY FIX: Store authentication in session
            storeContextValidator.setAuthenticatedUser(session, username.toUpperCase(), "CORPORATE");
            log.info("Successful Corporate login for user: {}", username);

            // SECURITY FIX: Return ONLY success status, NO user data in response
            Map<String, Object> secureResponse = new HashMap<>();
            secureResponse.put("success", true);
            secureResponse.put("message", "Login successful");
            return ResponseEntity.ok(secureResponse);
        } else {
            log.warn("Failed Corporate login attempt for username: {}", username);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @GetMapping("/rbmStores")
    public ResponseEntity<?> getStoresByRbm(
            @RequestParam String rbmUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;

            // If dates are not given, use cached data
            if (start == null && end == null) {
                StoreSummaryWrapperDTO cached = storeSummaryCache.get(rbmUsername);
                if (cached != null) {
                    return ResponseEntity.ok(new ApiResponse<>(200, "Fetched from cache", cached));
                }
            }

            // Else compute fresh
            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByRbmParallel(rbmUsername, start, end);

            // Cache only full summaries (i.e., without date filter)
            if (start == null && end == null) {
                storeSummaryCache.put(rbmUsername, result);
            }

            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched freshly", result));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error fetching data from sheet", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Unexpected error occurred", null));
        }
    }

    @GetMapping("/abmStores")
    public ResponseEntity<?> getStoresByAbm(
            @RequestParam String abmUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;

            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByAbmParallel(abmUsername, start, end);
            ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @GetMapping("/ceeStores")
    public ResponseEntity<?> getStoresByCee(
            @RequestParam String ceeUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;

            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByCeeParallel(ceeUsername, start, end);
            ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/corporateStores")
    public ResponseEntity<?> getStoresByCorporate(
            @RequestParam String corporateUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = (startDate != null) ? LocalDate.parse(startDate, formatter) : null;
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate, formatter) : null;

            StoreSummaryWrapperDTO result = tanishqPageService.fetchStoreSummariesByCorporateParallel(corporateUsername, start, end);
            ApiResponse<StoreSummaryWrapperDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", result);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Error fetching data from sheet", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            ApiResponse<String> error = new ApiResponse<>(500, "Unexpected error occurred", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/rbm/events")
    public ResponseEntity<?> getEventsByRbmUsername(@RequestParam String rbmUsername) {
        try {
            List<String> storeCodes = tanishqPageService.fetchStoresByRbm(rbmUsername);
            List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch events for RBM user.");
        }
    }

    @GetMapping("/abm/events")
    public ResponseEntity<?> getEventsByAbmUsername(@RequestParam String abmUsername) {
        try {
            List<String> storeCodes = tanishqPageService.fetchStoresByAbm(abmUsername);
            List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch events for ABM user.");
        }
    }

    @GetMapping("/cee/events")
    public ResponseEntity<?> getEventsByCeeUsername(@RequestParam String ceeUsername) {
        try {
            List<String> storeCodes = tanishqPageService.fetchStoresByCee(ceeUsername);
            List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch events for CEE user.");
        }
    }

    @GetMapping("/corporate/events")
    public ResponseEntity<?> getEventsByCorporateUsername(@RequestParam String corporateUsername) {
        try {
            List<String> storeCodes = tanishqPageService.fetchStoresByCorporate(corporateUsername);
            List<Map<String, Object>> events = tanishqPageService.getOnlyEventsForStores(storeCodes);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch events for Corporate user.");
        }
    }

    @GetMapping("/store/events/download")
    public void downloadStoreEventsAsCsv(
            @RequestParam String storeCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response,
            HttpSession session
    ) throws Exception {
        // SECURITY: Validate authentication and authorization
        if (!storeContextValidator.isAuthenticated(session)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication required. Please log in.");
            log.warn("Unauthorized CSV download attempt - no authentication");
            return;
        }

        if (!storeContextValidator.validateStoreAccess(session, storeCode)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access denied. You are not authorized to download data for this store.");
            log.error("SECURITY ALERT: Unauthorized CSV download attempt for store: {} by user: {}",
                     storeCode, storeContextValidator.getAuthenticatedUser(session));
            return;
        }

        log.info("Downloading events for storeCode: {}, startDate: {}, endDate: {}", storeCode, startDate, endDate);

        // Route CEE/ABM/RBM/CORP codes to their respective store lists
        List<String> storeCodes = new ArrayList<>();
        if (storeCode.contains("-CEE")) {
            storeCodes = tanishqPageService.fetchStoresByCee(storeCode);
        } else if (storeCode.contains("-ABM")) {
            storeCodes = tanishqPageService.fetchStoresByAbm(storeCode);
        } else if (storeCode.contains("-RBM")) {
            storeCodes = tanishqPageService.fetchStoresByRbm(storeCode);
        } else if (storeCode.contains("CORP-") || storeCode.contains("-CORP")) {
            storeCodes = tanishqPageService.fetchStoresByCorporate(storeCode);
        } else {
            storeCodes.add(storeCode);
        }
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);
        log.info("Found {} total events for store {}", allEvents.size(), storeCode);

        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
            log.info("After date filtering: {} events remain", filteredEvents.size());
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=store_events_" + storeCode + ".csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define custom column headers in the desired order (including Region)
        List<String> displayHeaders = Arrays.asList(
                "Created At", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "Completed Events Drive Link", "Community",
                "Location", "Attendees Uploaded", "Sale", "Advance", "GHS/RGA", "GMB",
                "Diamond Awareness", "GHS Flag"
        );

        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "region", "id", "event_type", "event_sub_type", "event_name", "rso",
                "start_date", "image", "invitees", "attendees", "completed_events_drive_link", "community",
                "location", "attendees_uploaded", "sale", "advance", "ghs_or_rga", "gmb",
                "diamond_awareness", "ghs_flag"
        );

        writer.writeNext(displayHeaders.toArray(new String[0]));

        log.info("Writing {} rows to CSV for store {}", filteredEvents.size(), storeCode);
        if (!filteredEvents.isEmpty()) {
            log.debug("Sample event data (first row keys): {}", filteredEvents.get(0).keySet());
        }

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = dbFields.stream()
                    .map(field -> {
                        Object value = row.get(field);
                        return value != null ? value.toString() : "";
                    })
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        log.info("CSV export completed successfully for store {}", storeCode);
        writer.flush();
        writer.close();
    }

    @GetMapping("/abm/events/download")
    public void downloadAbmEventsAsCsv(
            @RequestParam String abmUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        List<String> storeCodes = tanishqPageService.fetchStoresByAbm(abmUsername);
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);

        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define custom column headers in the desired order
        List<String> displayHeaders = Arrays.asList(
                "Created At", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "Completed Events Drive Link", "Community",
                "Location", "Attendees Uploaded", "Sale", "Advance", "GHS/RGA", "GMB",
                "Diamond Awareness", "GHS Flag"
        );

        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "region", "id", "event_type", "event_sub_type", "event_name", "rso",
                "start_date", "image", "invitees", "attendees", "completed_events_drive_link", "community",
                "location", "attendees_uploaded", "sale", "advance", "ghs_or_rga", "gmb",
                "diamond_awareness", "ghs_flag"
        );

        writer.writeNext(displayHeaders.toArray(new String[0]));

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = dbFields.stream()
                    .map(field -> row.getOrDefault(field, "").toString())
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        writer.flush();
        writer.close();
    }

    @GetMapping("/rbm/events/download")
    public void downloadRbmEventsAsCsv(
            @RequestParam String rbmUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        List<String> storeCodes = tanishqPageService.fetchStoresByRbm(rbmUsername);
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);

        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=rbm_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define custom column headers in the desired order
        List<String> displayHeaders = Arrays.asList(
                "Created At", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "Completed Events Drive Link", "Community",
                "Location", "Attendees Uploaded", "Sale", "Advance", "GHS/RGA", "GMB",
                "Diamond Awareness", "GHS Flag"
        );

        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "region", "id", "event_type", "event_sub_type", "event_name", "rso",
                "start_date", "image", "invitees", "attendees", "completed_events_drive_link", "community",
                "location", "attendees_uploaded", "sale", "advance", "ghs_or_rga", "gmb",
                "diamond_awareness", "ghs_flag"
        );

        writer.writeNext(displayHeaders.toArray(new String[0]));

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = dbFields.stream()
                    .map(field -> row.getOrDefault(field, "").toString())
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        writer.flush();
        writer.close();
    }


    @GetMapping("/cee/events/download")
    public void downloadCeeEventsAsCsv(
            @RequestParam String ceeUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        List<String> storeCodes = tanishqPageService.fetchStoresByCee(ceeUsername);
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);

        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=cee_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define custom column headers in the desired order
        List<String> displayHeaders = Arrays.asList(
                "Created At", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "Completed Events Drive Link", "Community",
                "Location", "Attendees Uploaded", "Sale", "Advance", "GHS/RGA", "GMB",
                "Diamond Awareness", "GHS Flag"
        );

        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "region", "id", "event_type", "event_sub_type", "event_name", "rso",
                "start_date", "image", "invitees", "attendees", "completed_events_drive_link", "community",
                "location", "attendees_uploaded", "sale", "advance", "ghs_or_rga", "gmb",
                "diamond_awareness", "ghs_flag"
        );

        writer.writeNext(displayHeaders.toArray(new String[0]));

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = dbFields.stream()
                    .map(field -> row.getOrDefault(field, "").toString())
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        writer.flush();
        writer.close();
    }

    @GetMapping("/corporate/events/download")
    public void downloadCorporateEventsAsCsv(
            @RequestParam String corporateUsername,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        List<String> storeCodes = tanishqPageService.fetchStoresByCorporate(corporateUsername);
        List<Map<String, Object>> allEvents = tanishqPageService.getOnlyEventsForStores(storeCodes);

        List<Map<String, Object>> filteredEvents;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            filteredEvents = tanishqPageService.filterEventsByStartDate(allEvents, startDate, endDate);
        } else {
            filteredEvents = allEvents;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=corporate_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define custom column headers in the desired order
        List<String> displayHeaders = Arrays.asList(
                "Created At", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "Completed Events Drive Link", "Community",
                "Location", "Attendees Uploaded", "Sale", "Advance", "GHS/RGA", "GMB",
                "Diamond Awareness", "GHS Flag"
        );

        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "region", "id", "event_type", "event_sub_type", "event_name", "rso",
                "start_date", "image", "invitees", "attendees", "completed_events_drive_link", "community",
                "location", "attendees_uploaded", "sale", "advance", "ghs_or_rga", "gmb",
                "diamond_awareness", "ghs_flag"
        );

        writer.writeNext(displayHeaders.toArray(new String[0]));

        for (Map<String, Object> row : filteredEvents) {
            List<String> rowData = dbFields.stream()
                    .map(field -> row.getOrDefault(field, "").toString())
                    .collect(Collectors.toList());
            writer.writeNext(rowData.toArray(new String[0]));
        }

        writer.flush();
        writer.close();
    }

    @GetMapping("/rbm/summary")
    public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getRbmSummary(
            @RequestParam String rbmUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            StoreEventSummaryDTO summary = tanishqPageService.getRbmSummary(rbmUsername, startDate, endDate);
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(200, "RBM summary fetched successfully", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(500, "Error fetching RBM summary: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/cee/summary")
    public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getCeeSummary(
            @RequestParam String ceeUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            StoreEventSummaryDTO summary = tanishqPageService.getCeeSummary(ceeUsername, startDate, endDate);
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(200, "CEE summary fetched successfully", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(500, "Error fetching CEE summary: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/abm/summary")
    public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getAbmSummary(
            @RequestParam String abmUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            StoreEventSummaryDTO summary = tanishqPageService.getAbmSummary(abmUsername, startDate, endDate);
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(200, "ABM summary fetched successfully", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(500, "Error fetching ABM summary: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/corporate/summary")
    public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getCorporateSummary(
            @RequestParam String corporateUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            StoreEventSummaryDTO summary = tanishqPageService.getCorporateSummary(corporateUsername, startDate, endDate);
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(200, "Corporate summary fetched successfully", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(500, "Error fetching Corporate summary: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/download-event-report")
    public ResponseEntity<InputStreamResource> downloadExcelFile() throws IOException {
        ClassPathResource file = new ClassPathResource("static/EventAttendedRepository.xlsx");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=EventAttendedRepository.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.contentLength())
                .body(new InputStreamResource(file.getInputStream()));
    }

    @GetMapping("/download-invitees-sample")
    public ResponseEntity<InputStreamResource> downloadInviteesSampleFile() throws IOException {
        ClassPathResource file = new ClassPathResource("static/contact.xlsx");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invitees_sample_format.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.contentLength())
                .body(new InputStreamResource(file.getInputStream()));
    }

    @GetMapping("/store-summary")
    public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getStoreSummary(
            @RequestParam String storeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            StoreEventSummaryDTO summary;
            // Route to appropriate service based on the code type
            if (storeCode.contains("-CEE")) {
                summary = tanishqPageService.getCeeSummary(storeCode, startDate, endDate);
            } else if (storeCode.contains("-ABM")) {
                summary = tanishqPageService.getAbmSummary(storeCode, startDate, endDate);
            } else if (storeCode.contains("-RBM")) {
                summary = tanishqPageService.getRbmSummary(storeCode, startDate, endDate);
            } else if (storeCode.contains("CORP-") || storeCode.contains("-CORP")) {
                summary = tanishqPageService.getCorporateSummary(storeCode, startDate, endDate);
            } else {
                summary = tanishqPageService.processSingleStoreCode(storeCode, startDate, endDate);
            }
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(500, "Error fetching store summary: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
