package com.dechub.tanishq.controller;

import com.dechub.tanishq.config.StoreSummaryCache;
import com.dechub.tanishq.dto.eventsDto.*;
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

import javax.servlet.http.HttpServletResponse;
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
    public EventsLoginResponseDTO eventsLogin(@RequestBody LoginDTO loginDTO) throws Exception {
        return tanishqPageService.eventsLogin(loginDTO.getCode(),loginDTO.getPassword());
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

    @PostMapping(path = "/upload", produces = "application/json")
    public QrResponseDTO storeEventsDetails(
            @RequestParam(value = "code",required = false) String code,
            @RequestParam(value = "file",required = false) MultipartFile file,
            @RequestParam(value ="description",required = false) String description,
            @RequestParam(value ="singalInvite",required = false) boolean isSingleCustomer,
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
            @RequestParam(value = "diamondAwareness", required = false, defaultValue = "false") boolean diamondAwareness,
            @RequestParam(value = "ghsFlag", required = false, defaultValue = "false") boolean ghsFlag


    ) {
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
    public CompletedEventsResponseDTO getAllCompletedEvents(@RequestBody storeCodeDataDTO storeCodeDataDTO){
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
    public ResponseDataDTO updateSaleOfAnEvent(@RequestParam String eventCode,@RequestParam String sale){
        return tanishqPageService.updateSaleOfAnEvent(eventCode,sale);
    }
    @PostMapping("/updateAdvanceOfAnEvent")
    public ResponseDataDTO updateAdvanceOfAnEvent(@RequestParam String eventCode,@RequestParam String advance){
        return tanishqPageService.updateAdvanceOfAnEvent(eventCode,advance);
    }
    @PostMapping("/updateGhsRgaOfAnEvent")
    public ResponseDataDTO updateGhsRgaOfAnEvent(@RequestParam String eventCode,@RequestParam String ghsRga){
        return tanishqPageService.updateGhsRgaOfAnEvent(eventCode,ghsRga);
    }
    @PostMapping("/updateGmbOfAnEvent")
    public ResponseDataDTO updateGmbOfAnEvent(@RequestParam String eventCode,@RequestParam String gmb){
        return tanishqPageService.updateGmbOfAnEvent(eventCode,gmb);
    }

    @PostMapping("/getinvitedmember")
    public ResponseEntity<ResponseDataDTO> getInvitedMember(@RequestParam String eventCode) throws Exception {
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
    public ResponseDataDTO changePassword(@RequestParam String storeCode,@RequestParam String oldPassword,@RequestParam String newPassword,@RequestParam String confirmPassword){
        return tanishqPageService.changePasswordForEventManager(storeCode,oldPassword,newPassword,confirmPassword);
    }

    @GetMapping("/getPasswordHint")
    public ResponseDataDTO getPasswordHint(@RequestParam String storeCode){
        return tanishqPageService.getPasswordHintForStore(storeCode);
    }

    @PostMapping("/abm_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginAbm(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
        }

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateAbm(username, password);

        if (user.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
        }
    }

    @PostMapping("/rbm_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginRbm(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
        }

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateRbm(username, password);

        if (user.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
        }
    }


    @PostMapping("/cee_login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginCee(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Username or password missing", null));
        }

        Optional<LoginResponseDTO> user = tanishqPageService.authenticateCee(username, password);

        if (user.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", user.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Invalid credentials", null));
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

    @GetMapping("/store/events/download")
    public void downloadStoreEventsAsCsv(
            @RequestParam String storeCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response
    ) throws Exception {
        log.info("Downloading events for storeCode: {}, startDate: {}, endDate: {}", storeCode, startDate, endDate);

        List<String> storeCodes = new ArrayList<>();
        storeCodes.add(storeCode);
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

        // Define custom column headers in the desired order (excluding region, import_version, is_active, is_visible)
        List<String> displayHeaders = Arrays.asList(
                "Created At", "Store Code", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "Completed Events Drive Link", "Community",
                "Location", "Attendees Uploaded", "Sale", "Advance", "GHS/RGA", "GMB",
                "Diamond Awareness", "GHS Flag"
        );
        
        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "id", "event_type", "event_sub_type", "event_name", "rso",
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
        response.setHeader("Content-Disposition", "attachment; filename=rbm_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define custom column headers in the desired order
        List<String> displayHeaders = Arrays.asList(
                "createdAt", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "completed Events", "Community",
                "location", "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb",
                "Drive link", "Diamond Awareness", "GHS"
        );
        
        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "region", "id", "event_type", "event_sub_type", "event_name", "rso",
                "start_date", "image", "invitees", "attendees", "completed_events_drive_link", "community",
                "location", "attendees_uploaded", "sale", "advance", "ghs_or_rga", "gmb",
                "completed_events_drive_link", "diamond_awareness", "ghs_flag"
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
        response.setHeader("Content-Disposition", "attachment; filename=abm_events.csv");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));

        // Define custom column headers in the desired order
        List<String> displayHeaders = Arrays.asList(
                "createdAt", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "completed Events", "Community",
                "location", "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb",
                "Drive link", "Diamond Awareness", "GHS"
        );
        
        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "region", "id", "event_type", "event_sub_type", "event_name", "rso",
                "start_date", "image", "invitees", "attendees", "completed_events_drive_link", "community",
                "location", "attendees_uploaded", "sale", "advance", "ghs_or_rga", "gmb",
                "completed_events_drive_link", "diamond_awareness", "ghs_flag"
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
                "createdAt", "Store Code", "Region", "Id", "Event Type", "Event Sub Type", "Event Name", "RSO",
                "Start Date", "Image", "Invitees", "Attendees", "completed Events", "Community",
                "location", "isAttendeesUploaded", "sale", "advance", "ghs/rga", "gmb",
                "Drive link", "Diamond Awareness", "GHS"
        );
        
        // Map custom headers to database field names
        List<String> dbFields = Arrays.asList(
                "created_at", "store_code", "region", "id", "event_type", "event_sub_type", "event_name", "rso",
                "start_date", "image", "invitees", "attendees", "completed_events_drive_link", "community",
                "location", "attendees_uploaded", "sale", "advance", "ghs_or_rga", "gmb",
                "completed_events_drive_link", "diamond_awareness", "ghs_flag"
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
    @GetMapping("/store-summary")
    public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getStoreSummary(
            @RequestParam String storeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            StoreEventSummaryDTO summary = tanishqPageService.processSingleStoreCode(storeCode, startDate, endDate);
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(200, "Store summary fetched successfully", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<StoreEventSummaryDTO> response = new ApiResponse<>(500, "Error fetching store summary: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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


    @GetMapping("/download-event-report")
    public ResponseEntity<InputStreamResource> downloadExcelFile() throws IOException {
        ClassPathResource file = new ClassPathResource("static/EventAttendedRepository.xlsx");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=EventAttendedRepository.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.contentLength())
                .body(new InputStreamResource(file.getInputStream()));
    }
}
